package com.juuls_trinkets.bar_scan.service.queuestatus;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juuls_trinkets.bar_scan.dto.QueueStatusResponseDTO;
import com.juuls_trinkets.bar_scan.exception.BadRequestException;
import com.juuls_trinkets.bar_scan.exception.ResourceNotFoundException;
import com.juuls_trinkets.bar_scan.model.AppUser;
import com.juuls_trinkets.bar_scan.model.Shop;
import com.juuls_trinkets.bar_scan.repository.QueueStatusRepository;
import com.juuls_trinkets.bar_scan.repository.ShopRepository;
import com.juuls_trinkets.bar_scan.repository.UserRepository;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;
import com.juuls_trinkets.bar_scan.service.authorization.UserAccessPolicy;

@Service
public class QueueStatusService implements QueueStatusApplicationService {

    private final QueueStatusRepository queueStatusRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final UserAccessPolicy userAccessPolicy;

    public QueueStatusService(
            QueueStatusRepository queueStatusRepository,
            ShopRepository shopRepository,
            UserRepository userRepository,
            UserAccessPolicy userAccessPolicy
    ) {
        this.queueStatusRepository = queueStatusRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.userAccessPolicy = userAccessPolicy;
    }

    @Transactional
    @Override
    public void registerExpectedMessages(UUID userId, UUID syncBatchId, List<UUID> messageIds) {
        AppUser user = findUser(userId);
        UUID shopId = requireShop(user.getShopId());
        messageIds.stream()
                .distinct()
                .forEach(messageId -> queueStatusRepository.registerExpected(messageId, syncBatchId, user.getCompanyId(), shopId));
    }

    @Transactional
    @Override
    public void cancelUnpublishedMessages(UUID userId, UUID syncBatchId, List<UUID> messageIds) {
        AppUser user = findUser(userId);
        UUID shopId = requireShop(user.getShopId());
        messageIds.stream()
                .distinct()
                .forEach(messageId -> queueStatusRepository.cancelUnpublished(messageId, syncBatchId, user.getCompanyId(), shopId));
    }

    @Override
    public QueueStatusResponseDTO getStatus(CustomUserDetails user, UUID shopId) {
        UUID readableShopId = resolveReadableShopId(user, shopId);
        return queueStatusRepository.findStatus(user.getCompanyId(), readableShopId);
    }

    @Override
    public List<QueueStatusResponseDTO> getVisibleStatuses(CustomUserDetails user) {
        String role = user.getAuthorities().iterator().next().getAuthority();
        if (userAccessPolicy.canAssignShopAndRole(role)) {
            return queueStatusRepository.findStatuses(user.getCompanyId());
        }
        return List.of(queueStatusRepository.findStatus(user.getCompanyId(), requireShop(user.getShopId())));
    }

    @Transactional
    @Override
    public boolean isProcessed(UUID messageId) {
        return queueStatusRepository.isProcessed(messageId);
    }

    @Transactional
    @Override
    public void markProcessed(UUID messageId, UUID syncBatchId, UUID companyId, UUID shopId) {
        queueStatusRepository.markProcessed(messageId, syncBatchId, companyId, shopId);
    }

    private AppUser findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for queue status event"));
    }

    private UUID resolveReadableShopId(CustomUserDetails user, UUID requestedShopId) {
        String role = user.getAuthorities().iterator().next().getAuthority();
        if (userAccessPolicy.canAssignShopAndRole(role)) {
            if (requestedShopId == null) {
                throw new BadRequestException("shopId is required");
            }
            Shop shop = shopRepository.findById(requestedShopId)
                    .filter(candidate -> candidate.getCompanyId().equals(user.getCompanyId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
            return shop.getId();
        }

        UUID ownShopId = requireShop(user.getShopId());
        if (requestedShopId != null && !requestedShopId.equals(ownShopId)) {
            throw new BadRequestException("This user cannot view that shop queue status");
        }
        return ownShopId;
    }

    private UUID requireShop(UUID shopId) {
        if (shopId == null) {
            throw new ResourceNotFoundException("User is not assigned to a shop");
        }
        return shopId;
    }
}

package com.koliving.api.room.application;

import static com.koliving.api.base.ServiceError.RECORD_NOT_EXIST;

import com.google.common.collect.Sets;
import com.koliving.api.base.ServiceError;
import com.koliving.api.base.exception.KolivingServiceException;
import com.koliving.api.location.domain.Location;
import com.koliving.api.location.infra.LocationRepository;
import com.koliving.api.room.application.dto.RoomResponse;
import com.koliving.api.room.application.dto.RoomSaveRequest;
import com.koliving.api.room.domain.Furnishing;
import com.koliving.api.room.domain.Room;
import com.koliving.api.room.infra.FurnishingRepository;
import com.koliving.api.room.infra.RoomRepository;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * author : haedoang date : 2023/08/26 description :
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {

    private final FurnishingRepository furnishingRepository;
    private final LocationRepository locationRepository;
    private final RoomRepository roomRepository;

    public List<RoomResponse> list() {
        return roomRepository.findAll()
            .stream()
            .map(RoomResponse::valueOf)
            .collect(Collectors.toList());
    }

    @Transactional
    public Long save(RoomSaveRequest request) {
        final Room room = roomRepository.save(
            request.toEntity(
                getLocationById(request.locationId()),
                getFurnishingsByIds(request.furnishingIds())
            )
        );

        return room.getId();
    }

    private Set<Furnishing> getFurnishingsByIds(Set<Long> furnishingIds) {
        if (CollectionUtils.isEmpty(furnishingIds)) {
            return Collections.emptySet();
        }

        final List<Furnishing> furnishings = furnishingRepository.findAllById(furnishingIds);

        if (furnishings.size() != furnishingIds.size()) {
            throw new KolivingServiceException(ServiceError.RECORD_NOT_EXIST);
        }

        return Sets.newHashSet(furnishings);
    }

    private Location getLocationById(Long locationId) {
        final Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new KolivingServiceException(RECORD_NOT_EXIST));

        if (location.getLocationType().isTopLocation()) {
            throw new KolivingServiceException(ServiceError.INVALID_LOCATION);
        }

        return location;
    }

}
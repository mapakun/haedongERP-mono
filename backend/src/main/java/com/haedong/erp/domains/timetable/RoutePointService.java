package com.haedong.erp.domains.timetable;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.domains.timetable.dto.RoutePointResponse;
import com.haedong.erp.domains.timetable.dto.RoutePointSaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoutePointService {

    private final RoutePointMapper routePointMapper;

    @Transactional(readOnly = true)
    public List<RoutePointResponse> findByRouteNo(String routeNo) {
        return routePointMapper.findByRouteNo(routeNo);
    }

    /** 장소 이름을 통째로 맞춘다. 요청에 없거나 이름이 비어 있는 위치는 지워진다 */
    @Transactional
    public void save(String routeNo, RoutePointSaveRequest request) {
        routePointMapper.lockRoute(routeNo)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "노선을 찾을 수 없습니다."));

        Set<PointKey> seen = EnumSet.noneOf(PointKey.class);
        for (RoutePointSaveRequest.Item item : request.points()) {
            if (!seen.add(item.pointKey())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "같은 위치가 두 번 들어 있습니다.");
            }
        }

        routePointMapper.deleteByRouteNo(routeNo);
        for (RoutePointSaveRequest.Item item : request.points()) {
            String name = item.name() == null ? "" : item.name().strip();
            if (!name.isEmpty()) {
                routePointMapper.insert(routeNo, item.pointKey(), name);
            }
        }
    }
}
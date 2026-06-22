package gcy.system.service.admin;

import gcy.system.entity.dto.Result;

public interface IDashboardService {

    Result getStats();

    Result getOrderTrend();

    Result getLowStock();

    Result getTopFurniture();
}

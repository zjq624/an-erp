package com.springmvc.controller;

import com.springmvc.dto.MaterialStockCostRecord;
import com.springmvc.dto.PageMode;
import com.springmvc.service.StockService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/stock")
public class StockController {

    @Resource
    StockService stockService;

    @RequestMapping(value = "/searchMaterialCost", method = RequestMethod.POST)
    @ResponseBody
    public PageMode<MaterialStockCostRecord> searchMaterialCost(@RequestParam Integer current, @RequestParam Integer limit,
                                                                String sortColumn, String sort, String searchKey) {
        return stockService.pageMaterialCost(current, limit, sortColumn, sort, searchKey);
    }
}

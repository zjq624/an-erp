package com.springmvc.service;

import com.springmvc.dao.*;
import com.springmvc.dto.*;
import com.springmvc.exception.BadRequestException;
import com.springmvc.pojo.*;
import com.springmvc.utils.ParamUtils;
import com.springmvc.utils.RequestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("WarehouseService")
@Transactional
public class WarehouseService extends BaseService {

    //DAO变量声明
    @Resource
    private WarehouseDAO warehouseDAO;

    @Resource
    private MaterialInstockBillMaterialDAO materialInstockBillMaterialDAO;

    @Resource
    private MaterialOutstockBillMaterialDAO materialOutstockBillMaterialDAO;

    @Resource
    private ProductInstockBillProductDAO productInstockBillProductDAO;

    @Resource
    private ProductOutstockBillProductDAO productOutstockBillProductDAO;

    /**
     * 获取仓库表的所有信息
     *
     * @return 返回列表
     */
    public List<Warehouse> getList() {
        return warehouseDAO.selectByExample(new WarehouseQuery());
    }

    /**
     * 查询仓库资料信息（以列表形式显示）
     *
     * 取出主表信息:warehouse
     * 搜索字段：名字/NO
     * 筛选字段：无
     * 过滤信息：无
     *
     *
     * @param current           当前页数
     * @param limit             每页显示的页数
     * @param sortColumn        选择以哪一列排序
     * @param sort              正序/反序
     * @param searchKey         搜索的关键字
     * @return
     */
    public PageMode<Warehouse> pageWarehouse(@RequestParam Integer current, @RequestParam Integer limit,
                                           String sortColumn, String sort, String searchKey){
        WarehouseQuery warehouseQuery = new WarehouseQuery();
        warehouseQuery.setOffset((current-1) * limit);
        warehouseQuery.setLimit(limit);

        //如果那一列不为空的话,利用该函数将驼峰命名法-》下划线命名法
        if (!ParamUtils.isNull(sortColumn)){
            warehouseQuery.setOrderByClause(ParamUtils.camel2Underline(sortColumn) + " " + sort);
        }

        //搜索编号_模糊查询
        WarehouseQuery.Criteria criteria = warehouseQuery.or();
        if (!ParamUtils.isNull(searchKey)){
            criteria.andWarehouseNoLike("%" + searchKey + "%");
        }
        //搜索名字_模糊查询
        criteria = warehouseQuery.or();
        if (!ParamUtils.isNull(searchKey)) {
            criteria.andWarehouseNameLike("%" + searchKey + "%");
        }
        List<Warehouse> result = warehouseDAO.selectByExample(warehouseQuery);

        return new PageMode<Warehouse>(result, warehouseDAO.countByExample(warehouseQuery));
    }

    /**
     * 查询用户信息 （备用）（万一你要来一个点击列表查看详细信息的话）
     *
     * 将主表信息取出：warehouse
     *
     * @param warehouseId 数据库的ID值
     * @return
     */
    public Warehouse getWarehouseById(int warehouseId){
        Warehouse warehouse = warehouseDAO.selectByPrimaryKey(warehouseId);
        return warehouse;
    }


    /**
     * 增添仓库信息
     *
     * 将主表信息保存：warehouse
     * 添加日志信息：LogType.WAREHOUSE, Operate.ADD
     *
     * @param warehouseNo       NO
     * @param warehouseName     name
     * @return
     */
    public Warehouse addWarehouse(String warehouseNo, String warehouseName){

        //保存主表信息
        Admin loginAdmin = RequestUtils.getLoginAdminFromCache();
        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseNo(warehouseNo);
        warehouse.setWarehouseName(warehouseName);
        warehouse.setCreateAt(new Date());
        warehouse.setCreateBy(loginAdmin.getAdminId());
        warehouse.setUpdateAt(new Date());
        warehouse.setUpdateBy(loginAdmin.getAdminId());

        warehouseDAO.insertSelective(warehouse);

        //添加日志信息
        addLog(LogType.WAREHOUSE,Operate.ADD,warehouse.getWarehouseId());
        return getWarehouseById(warehouse.getWarehouseId());
    }

    /**
     * 修改仓库信息
     *
     * 更新主表warehouse
     * 添加日志:LogType.WAREHOUSE Operate.UPDATE
     *
     * @param warehouseNo
     * @param warehouseName
     * @return
     */
    public Warehouse updateWarehouse(Integer warehouseId, String warehouseNo, String warehouseName){

        //更新信息
        Admin loginAdmin = RequestUtils.getLoginAdminFromCache();
        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseId(warehouseId);
        warehouse.setWarehouseNo(warehouseNo);
        warehouse.setWarehouseName(warehouseName);
        warehouse.setUpdateAt(new Date());
        warehouse.setUpdateBy(loginAdmin.getAdminId());

        warehouseDAO.updateByPrimaryKeySelective(warehouse);

        //添加日志信息
        addLog(LogType.WAREHOUSE, Operate.UPDATE, warehouse.getWarehouseId());
        return getWarehouseById(warehouse.getWarehouseId());
    }

    /**
     * 删除仓库
     *
     * 删除的主表信息：warehouse
     * 查看有没有被别的表所引用：material_instock_bill_material / material_outstock_bill_material
     *                product_instock_bill_product / product_outstock_bill_prpduct
     * 添加日志信息：LogType.WAREHOUSE，Operate.REMOVE
     *
     * @param idList 我们从idList的string格式转换成的数列格式
     */
    public void removeWarehouse(List<Integer> idList){
        //检查是否被material_instock_bill_material引用
        MaterialInstockBillMaterialQuery materialInstockBillMaterialQuery = new MaterialInstockBillMaterialQuery();
        materialInstockBillMaterialQuery.or().andWarehouseIn(idList);
        if (materialInstockBillMaterialDAO.countByExample(materialInstockBillMaterialQuery) > 0){
            throw new BadRequestException(WAREHOUSE_REFER_BY_MATERIAL_INSTOCK_BILL_MATERIAL);
        }

        //检查是否被material_outstock_bill_material引用
        MaterialOutstockBillMaterialQuery materialOutstockBillMaterialQuery = new MaterialOutstockBillMaterialQuery();
        materialOutstockBillMaterialQuery.or().andWarehouseIn(idList);
        if (materialOutstockBillMaterialDAO.countByExample(materialOutstockBillMaterialQuery) > 0){
            throw new BadRequestException(WAREHOUSE_REFER_BY_MATERIAL_OUTSTOCK_BILL_MATERIAL);
        }

        //检查是否被product_instock_bill_product引用
        ProductInstockBillProductQuery productInstockBillProductQuery = new ProductInstockBillProductQuery();
        productInstockBillProductQuery.or().andWarehouseIn(idList);
        if (productInstockBillProductDAO.countByExample(productInstockBillProductQuery) > 0){
            throw new BadRequestException(WAREHOUSE_REFER_BY_PRODUCT_INSTOCK_BILL_PRODUCT);
        }

        //检查是否被product_outstock_bill_prpduct引用
        ProductOutstockBillProductQuery productOutstockBillProductQuery = new ProductOutstockBillProductQuery();
        productOutstockBillProductQuery.or().andWarehouseIn(idList);
        if(productOutstockBillProductDAO.countByExample(productOutstockBillProductQuery)> 0 ){
            throw new BadRequestException(WAREHOUSE_REFER_BY_PRODUCT_OUTSTOCK_BILL_PRPDUCT);
        }

        //删除主表Warehouse
        WarehouseQuery warehouseQuery = new WarehouseQuery();
        warehouseQuery.or().andWarehouseIdIn(idList);
        warehouseDAO.deleteByExample(warehouseQuery);

        //添加日志
        addLog(LogType.WAREHOUSE, Operate.REMOVE, idList);
    }

    private static final String WAREHOUSE_NAME_EXIST = "该仓库名已存在";
    private static final String WAREHOUSE_NO_EXIST = "该仓库编号已存在";
    private static final String WAREHOUSE_REFER_BY_MATERIAL_INSTOCK_BILL_MATERIAL = "仓库被物料入库单所引用";
    private static final String WAREHOUSE_REFER_BY_MATERIAL_OUTSTOCK_BILL_MATERIAL = "仓库被物料出库单所引用";
    private static final String WAREHOUSE_REFER_BY_PRODUCT_INSTOCK_BILL_PRODUCT = "仓库被产品入库单所引用";
    private static final String WAREHOUSE_REFER_BY_PRODUCT_OUTSTOCK_BILL_PRPDUCT = "仓库被产品出库单所引用";
}

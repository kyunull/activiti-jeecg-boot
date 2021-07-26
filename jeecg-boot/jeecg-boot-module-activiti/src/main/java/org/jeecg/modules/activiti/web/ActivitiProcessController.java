package org.jeecg.modules.activiti.web;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.activiti.entity.ActNode;
import org.jeecg.modules.activiti.entity.ActZprocess;
import org.jeecg.modules.activiti.entity.ProcessNodeVo;
import org.jeecg.modules.activiti.service.Impl.ActBusinessServiceImpl;
import org.jeecg.modules.activiti.service.Impl.ActNodeServiceImpl;
import org.jeecg.modules.activiti.service.Impl.ActZprocessServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程定义
 *
 * @author: dongjb
 * @date: 2021/5/27
 */
@RestController
@RequestMapping("/activiti_process")
@Slf4j
@Transactional(rollbackFor = Exception.class)
@Api(tags = "工作流-流程定义")
public class ActivitiProcessController {
    private final RepositoryService repositoryService;
    private final ActZprocessServiceImpl actZprocessService;
    private final ActNodeServiceImpl actNodeService;
    private final ActBusinessServiceImpl actBusinessService;

    @Autowired
    public ActivitiProcessController(RepositoryService repositoryService,
                                     ActZprocessServiceImpl actZprocessService,
                                     ActNodeServiceImpl actNodeService,
                                     ActBusinessServiceImpl actBusinessService) {
        this.repositoryService = repositoryService;
        this.actZprocessService = actZprocessService;
        this.actNodeService = actNodeService;
        this.actBusinessService = actBusinessService;
    }

    public static final String SPLIT_FLAG = ",";

    /**
     * 按条件获取可用流程列表
     *
     * @param processName 流程名称
     * @param processKey  流程key
     * @param isNew       是否最新
     * @param status      流程状态
     * @param roles       用户角色
     * @param request     http请求
     * @return 流程列表
     */
    @AutoLog(value = "获取流程定义列表")
    @ApiOperation(value = "获取流程定义列表", notes = "获取流程定义列表，通过act_z_process流程扩展表查询")
    @RequestMapping(value = "/listData", method = RequestMethod.GET)
    public Result<List<ActZprocess>> listData(@ApiParam(value = "流程名称") String processName,
                                              @ApiParam(value = "流程key") String processKey,
                                              @ApiParam(value = "是否最新") Boolean isNew,
                                              @ApiParam(value = "流程状态 部署后默认1激活") String status,
                                              @ApiParam(value = "如果此项不为空，则会过滤当前用户的角色权限") Boolean roles,
                                              HttpServletRequest request) {
        LambdaQueryWrapper<ActZprocess> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ActZprocess::getSort).orderByDesc(ActZprocess::getVersion);
        if (StrUtil.isNotBlank(processName)) {
            wrapper.like(ActZprocess::getName, processName);
        }
        if (StrUtil.isNotBlank(processKey)) {
            wrapper.like(ActZprocess::getProcessKey, processKey);
        }
        if (isNew != null && isNew) {
            wrapper.eq(ActZprocess::getLatest, 1);
        }
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(ActZprocess::getStatus, status);
        }
        String statuss = request.getParameter("statuss");
        if (StrUtil.isNotBlank(statuss)) {
            wrapper.in(ActZprocess::getStatus, statuss);
        }
        List<ActZprocess> list = actZprocessService.list(wrapper);
        //过滤角色
        if (roles != null && roles) {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            List<String> roleByUserName = actNodeService.getRoleByUserName(sysUser.getUsername());
            list = list.stream().filter(p -> {
                String pRoles = p.getRoles();
                if (StrUtil.isBlank(pRoles)) {
                    return true; //未设置授权的所有人都能用
                } else {
                    String[] split = pRoles.split(",");
                    for (String role : split) {
                        if (roleByUserName.contains(role)) {
                            return true;
                        }
                    }
                }
                return false;
            }).collect(Collectors.toList());

        }
        return Result.OK(list);
    }

    /**
     * 激活或挂起流程定义
     *
     * @param id     流程定义标识
     * @param status 激活或挂起状态
     * @return 修改状态是否成功
     */
    @AutoLog(value = "激活或挂起流程定义")
    @ApiOperation(value = "激活或挂起流程定义",
            notes = "更新流程定义表ACT_RE_PROCDEF，挂起字段SUSPENSION_STATE_。同时更新act_z_process的status字段")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    public Result<String> updateStatus(String id, Integer status) {

        ActZprocess actProcess = actZprocessService.getById(id);
        if (status == 1) {
            //启动前检查一下 业务表单路由，业务表名等信息是否齐全
            String routeName = actProcess.getRouteName();
            String businessTable = actProcess.getBusinessTable();
            if (StrUtil.isBlank(routeName) || StrUtil.isBlank(businessTable)) {
                return Result.error("未设置关联表单，点击编辑设置。", "");
            }

            repositoryService.activateProcessDefinitionById(id, true, new Date());
        } else {
            repositoryService.suspendProcessDefinitionById(id, true, new Date());
        }
        actProcess.setStatus(status);
        actZprocessService.updateById(actProcess);
        return Result.OK("修改成功！");
    }

    /**
     * 通过id删除流程定义
     *
     * @param proDefids 流程定义标识字符串，逗号分隔
     * @return 是否删除成功
     */
    @AutoLog(value = "删除流程定义")
    @ApiOperation(value = "删除流程定义", notes = "通过流程定义标识删除流程定义。已经存在流程实例不能删除," +
            "删除act_z_process的同时，级联删除act_z_node, ACT_RE_DEPLOYMENT" +
            "然后设置act_z_process 中最大版本记录为最新的记录")
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    public Result<String> delByIds(@RequestParam("ids") String proDefids) {
        for (String proDefid : proDefids.split(SPLIT_FLAG)) {
            if (CollectionUtil.isNotEmpty(actBusinessService.findByProcDefId(proDefid))) {
                return Result.error("包含已发起申请的流程，无法删除",null);
            }
            ActZprocess actProcess = actZprocessService.getById(proDefid);
            // 当删除最后一个版本时 删除关联数据
            if (actProcess == null) {
                return Result.error("该数据已删除！",null);
            }
            if (actProcess.getVersion() == 1) {
                deleteNodeUsers(proDefid);
            }
            // 级联删除
            repositoryService.deleteDeployment(actProcess.getDeploymentId(), true);
            actZprocessService.removeById(proDefid);
            // 更新最新版本
            actZprocessService.setLatestByProcessKey(actProcess.getProcessKey());
        }
        return Result.OK("删除成功");
    }

    public void deleteNodeUsers(String proDefid) {

        BpmnModel bpmnModel = repositoryService.getBpmnModel(proDefid);
        List<Process> processes = bpmnModel.getProcesses();
        for (Process process : processes) {
            Collection<FlowElement> elements = process.getFlowElements();
            for (FlowElement element : elements) {
                actNodeService.deleteByNodeId(element.getId(), proDefid);
            }
        }
    }

    /**
     * 流程定义转化为模型
     *
     * @param id 流程定义标识
     * @return 模型
     */
    @AutoLog(value = "流程定义转化为模型")
    @ApiOperation(value = "流程定义转化为模型", notes = "流程定义转化为模型")
    @RequestMapping(value = "/convertToModel", method = RequestMethod.POST)
    public Result<String> convertToModel(String id) {

        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        InputStream bpmnStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
        ActZprocess actProcess = actZprocessService.getById(id);

        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            InputStreamReader in = new InputStreamReader(bpmnStream, StandardCharsets.UTF_8);
            XMLStreamReader xtr = xif.createXMLStreamReader(in);
            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
            BpmnJsonConverter converter = new BpmnJsonConverter();

            ObjectNode modelNode = converter.convertToJson(bpmnModel);
            Model modelData = repositoryService.newModel();
            modelData.setKey(pd.getKey());
            modelData.setName(pd.getResourceName());

            ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, actProcess.getName());
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, modelData.getVersion());
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, actProcess.getDescription());
            modelData.setMetaInfo(modelObjectNode.toString());

            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("转化流程为模型失败", null);
        }
        return Result.OK("修改成功");
    }

    /**
     * 更新流程定义信息
     * @param actProcess 流程定义业务扩展对象
     * @return 是否修改成功
     */
    @AutoLog(value = "更新流程定义信息")
    @ApiOperation(value = "更新流程定义信息", notes = "更新流程定义信息，同时更新部署表和流程定义表的类别")
    @RequestMapping(value = "/updateInfo", method = RequestMethod.POST)
    public Result<String> updateInfo(ActZprocess actProcess) {

        ProcessDefinition pd = repositoryService.getProcessDefinition(actProcess.getId());
        if (pd == null) {
            return Result.error("流程定义不存在", null);
        }
        if (StrUtil.isNotBlank(actProcess.getCategoryId())) {
            repositoryService.setProcessDefinitionCategory(actProcess.getId(), actProcess.getCategoryId());
            repositoryService.setDeploymentCategory(pd.getDeploymentId(), actProcess.getCategoryId());
        }
        actZprocessService.updateById(actProcess);
        return Result.OK("修改成功");
    }

    /**
     * 通过流程定义id获取流程节点
     *
     * @param id 流程定义id
     * @return 流程节点
     */
    @AutoLog(value = "获取流程定义的全部节点")
    @ApiOperation(value = "获取全部节点", notes = "通过流程定义id获取流程节点,并从相应的节点中取出指派对象及变量")
    @RequestMapping(value = "/getProcessNode", method = RequestMethod.POST)
    public Result<List<ProcessNodeVo>> getProcessNode(String id) {

        BpmnModel bpmnModel = repositoryService.getBpmnModel(id);

        List<ProcessNodeVo> list = new ArrayList<>();

        List<Process> processes = bpmnModel.getProcesses();
        if (processes == null || processes.size() == 0) {
            return Result.OK();
        }
        for (Process process : processes) {
            Collection<FlowElement> elements = process.getFlowElements();
            for (FlowElement element : elements) {
                ProcessNodeVo node = new ProcessNodeVo();
                node.setProcDefId(id);
                node.setId(element.getId());
                node.setTitle(element.getName());
                if (element instanceof StartEvent) {
                    // 开始节点
                    node.setType(0);
                    node.setTitle("开始");
                } else if (element instanceof UserTask) {
                    // 用户任务
                    node.setType(1);
                    // 设置关联用户
                    node.setUsers(actNodeService.findUserByNodeId(element.getId(), id));
                    // 设置关联角色
                    node.setRoles(actNodeService.findRoleByNodeId(element.getId(), id));
                    // 设置关联部门
                    node.setDepartments(actNodeService.findDepartmentByNodeId(element.getId(), id));
                    // 设置关联部门负责人
                    node.setDepartmentManages(actNodeService.findDepartmentManageByNodeId(element.getId(), id));
                    // 是否设置发起人部门负责人
                    node.setChooseDepHeader(actNodeService.hasChooseDepHeader(element.getId(), id));
                    // 是否设置发起人
                    node.setChooseSponsor(actNodeService.hasChooseSponsor(element.getId(), id));

                    // 设置表单变量
                    StringBuilder variable = new StringBuilder();
                    List<String> formVariables = actNodeService.findFormVariableByNodeId(element.getId(), id);
                    for (String formVariable : formVariables) {
                        variable.append(formVariable).append(",");
                    }
                    node.setFormVariables(variable.length() > 0 ? variable.substring(0, variable.length() - 1) : variable.toString());
                } else if (element instanceof EndEvent) {
                    // 结束
                    node.setType(2);
                    node.setTitle("结束");
                } else {
                    // 排除其他连线或节点
                    continue;
                }
                list.add(node);
            }
        }
        list.sort(Comparator.comparing(ProcessNodeVo::getType));
        return Result.OK(list);
    }

    /**
     * 编辑节点分配用户
     *
     * @param nodeId              节点标识
     * @param procDefId           流程定义标识
     * @param userIds             用户标识列表
     * @param roleIds             角色标识列表
     * @param departmentIds       部门标识列表
     * @param departmentManageIds 部门管理标识列表
     * @param formVariables       表单变量列表
     * @param chooseDepHeader     操作人的部门负责人
     * @param chooseSponsor       发起人
     * @return 成功失败
     */
    @AutoLog(value = "编辑节点指派对象及变量")
    @ApiOperation(value = "编辑节点", notes = "编辑节点指派对象及变量,先清除节点的全部指派对象，然后分类插入")
    @RequestMapping(value = "/editNodeUser", method = RequestMethod.POST)
    public Result<String> editNodeUser(String nodeId, String procDefId, String userIds, String roleIds, String departmentIds, String departmentManageIds, String formVariables, Boolean chooseDepHeader, Boolean chooseSponsor) {

        // 删除其关联权限
        actNodeService.deleteByNodeId(nodeId, procDefId);
        // 分配新用户
        for (String userId : userIds.split(SPLIT_FLAG)) {
            ActNode actNode = new ActNode();
            actNode.setProcDefId(procDefId);
            actNode.setNodeId(nodeId);
            actNode.setRelateId(userId);
            actNode.setType(1);
            actNodeService.save(actNode);
        }
        // 分配新角色
        for (String roleId : roleIds.split(SPLIT_FLAG)) {
            ActNode actNode = new ActNode();
            actNode.setProcDefId(procDefId);
            actNode.setNodeId(nodeId);
            actNode.setRelateId(roleId);
            actNode.setType(0);
            actNodeService.save(actNode);
        }
        // 分配新部门
        for (String departmentId : departmentIds.split(SPLIT_FLAG)) {
            ActNode actNode = new ActNode();
            actNode.setProcDefId(procDefId);
            actNode.setNodeId(nodeId);
            actNode.setRelateId(departmentId);
            actNode.setType(2);
            actNodeService.save(actNode);
        }
        // 分配新部门负责人
        for (String departmentId : departmentManageIds.split(SPLIT_FLAG)) {
            ActNode actNode = new ActNode();
            actNode.setProcDefId(procDefId);
            actNode.setNodeId(nodeId);
            actNode.setRelateId(departmentId);
            actNode.setType(5);
            actNodeService.save(actNode);
        }

        // 表单变量
        for (String formVariable : formVariables.split(SPLIT_FLAG)) {
            ActNode actNode = new ActNode();
            actNode.setProcDefId(procDefId);
            actNode.setNodeId(nodeId);
            actNode.setRelateId(formVariable);
            actNode.setType(6);
            actNodeService.save(actNode);
        }

        if (chooseDepHeader != null && chooseDepHeader) {
            ActNode actNode = new ActNode();
            actNode.setProcDefId(procDefId);
            actNode.setNodeId(nodeId);
            actNode.setType(4);
            actNodeService.save(actNode);
        }
        if (chooseSponsor != null && chooseSponsor) {
            ActNode actNode = new ActNode();
            actNode.setProcDefId(procDefId);
            actNode.setNodeId(nodeId);
            actNode.setType(3);
            actNodeService.save(actNode);
        }
        return Result.OK("操作成功");
    }

    @AutoLog(value = "获取下个节点")
    @ApiOperation(value = "获取下个节点", notes = "通过当前节点定义id获取下一个节点")
    @RequestMapping(value = "/getNextNode", method = RequestMethod.GET)
    public Result<ProcessNodeVo> getNextNode(@ApiParam("流程定义id") String procDefId,
                                             @ApiParam("当前节点定义id") String currActId,
                                             @ApiParam("流程实例id") String procInstId) {
        ProcessNodeVo node = actZprocessService.getNextNode(procDefId, currActId, procInstId);
        return Result.OK(node);
    }

    @AutoLog(value = "获取当前节点")
    @ApiOperation(value = "获取当前节点", notes = "通过节点nodeId获取审批人")
    @RequestMapping(value = "/getNode", method = RequestMethod.GET)
    public Result<ProcessNodeVo> getNode(@ApiParam("节点nodeId") @RequestParam("nodeId") String nodeId,
                                         @ApiParam("表单名称") @RequestParam("tableName") String tableName,
                                         @ApiParam("表单id") @RequestParam("tableId") String tableId) {
        ProcessNodeVo node = actZprocessService.getNode(nodeId, tableName, tableId);
        return Result.OK(node);
    }

    @AutoLog(value = "获取最新部署的流程定义")
    @ApiOperation(value = "获取最新部署的流程定义", notes = "获取最新部署的流程定义")
    @RequestMapping(value = "/queryNewestProcess", method = RequestMethod.GET)
    public Result<List<ActZprocess>> queryNewestProcess(@ApiParam("流程定义key") @RequestParam(value = "processKey", defaultValue = "") String processKey) {
        List<ActZprocess> actZprocesses = actZprocessService.queryNewestProcess(processKey);
        return Result.OK(actZprocesses);
    }
}

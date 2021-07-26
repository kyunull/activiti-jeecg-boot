package org.jeecg.modules.activiti.web;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.explorer.util.XmlUtil;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.activiti.entity.ActZprocess;
import org.jeecg.modules.activiti.service.Impl.ActZprocessServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 模型设计
 *
 * @author: dongjb
 * @date: 2021/5/26
 */
@RestController
@RequestMapping("/activiti/models")
@Slf4j
@Api(tags = "工作流-模型设计", description = "流程模型Model操作相关")
public class ActivitiModelController {
    private final RepositoryService repositoryService;
    private final HistoryService historyService;
    private final RuntimeService runtimeService;
    private final ProcessEngineConfiguration processEngineConfiguration;
    private final TaskService taskService;
    private final ObjectMapper objectMapper;
    private final ActZprocessServiceImpl actZprocessService;

    @Autowired
    public ActivitiModelController(RepositoryService repositoryService,
                                   HistoryService historyService,
                                   RuntimeService runtimeService,
                                   ProcessEngineConfiguration processEngineConfiguration,
                                   TaskService taskService,
                                   ObjectMapper objectMapper,
                                   ActZprocessServiceImpl actZprocessService) {
        this.repositoryService = repositoryService;
        this.historyService = historyService;
        this.runtimeService = runtimeService;
        this.processEngineConfiguration = processEngineConfiguration;
        this.taskService = taskService;
        this.objectMapper = objectMapper;
        this.actZprocessService = actZprocessService;
    }

    public static final String NC_NAME = "NCName";
    public static final String PRIMARY = "PRIMARY";

    /**
     * 获取模型列表
     *
     * @param request keyWord 模型名称 like
     * @return 按条件或者全部模型
     */
    @AutoLog(value = "获取模型列表")
    @ApiOperation(value = "模型列表", notes = "keyWord不空则匹配模型名称，否则查询全部")
    @RequestMapping(value = "/modelListData", method = RequestMethod.GET)
    @ResponseBody
    public Result<Object> modelListData(HttpServletRequest request) {
        log.info("-------------模型列表-------------");
        ModelQuery modelQuery = repositoryService.createModelQuery();
        //搜索关键字
        String keyWord = request.getParameter("keyWord");
        if (StrUtil.isNotBlank(keyWord)) {
            modelQuery.modelNameLike("%" + keyWord + "%");
        }
        List<Model> models = modelQuery.orderByCreateTime().desc().list();

        return Result.OK(models);
    }

    /**
     * 新建模型
     *
     * @param request  http 请求
     * @param response http 响应
     */
    @AutoLog(value = "新建模型")
    @ApiOperation(value = "新建模型", notes = "新建模型")
    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public void newModel(HttpServletRequest request, HttpServletResponse response) {
        try {
            //初始化一个空模型
            Model model = repositoryService.newModel();
            //设置一些默认信息
            int revision = 1;
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String key = request.getParameter("key");
            if (StrUtil.isBlank(name)) {
                name = "new-process";
            }
            if (StrUtil.isBlank(description)) {
                description = "description";
            }
            if (StrUtil.isBlank(key)) {
                key = "processKey";
            }

            ObjectNode modelNode = objectMapper.createObjectNode();
            modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
            modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

            model.setName(name);
            model.setKey(key);
            model.setMetaInfo(modelNode.toString());

            repositoryService.saveModel(model);
            String id = model.getId();

            //完善ModelEditorSource
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            repositoryService.addModelEditorSource(id, editorNode.toString().getBytes(StandardCharsets.UTF_8));
            response.sendRedirect(request.getContextPath() + "/activiti/modeler.html?modelId=" + id);
        } catch (IOException e) {
            e.printStackTrace();
            log.info("模型创建失败！");
        }
    }

    @AutoLog(value = "上传一个已有模型")
    @ApiOperation(value = "上传模型", notes = "上传一个已有模型，bpmn的xml文件")
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public Result<Object> deployUploadedFile(@RequestParam("uploadfile") MultipartFile uploadfile) {
        InputStreamReader in = null;
        if (uploadfile == null) {
            return Result.error("上传模型不能为空");
        }
        String fileName = uploadfile.getOriginalFilename();
        if (fileName == null) {
            return Result.error("文件名不能为空");
        }
        if (!(fileName.endsWith(".bpmn20.xml") || fileName.endsWith(".bpmn"))) {
            return Result.error("文件类型错误");
        }
        try {
            XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
            in = new InputStreamReader(new ByteArrayInputStream(uploadfile.getBytes()), StandardCharsets.UTF_8);
            XMLStreamReader xtr = xif.createXMLStreamReader(in);
            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

            if (bpmnModel.getMainProcess() == null || bpmnModel.getMainProcess().getId() == null) {
                System.out.println("err1");
            } else {
                if (bpmnModel.getLocationMap().isEmpty()) {
                    System.out.println("err2");
                } else {
                    String processName;
                    if (StringUtils.isNotEmpty(bpmnModel.getMainProcess().getName())) {
                        processName = bpmnModel.getMainProcess().getName();
                    } else {
                        processName = bpmnModel.getMainProcess().getId();
                    }
                    Model modelData;
                    modelData = repositoryService.newModel();
                    ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
                    modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processName);
                    modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
                    modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, processName);
                    modelData.setMetaInfo(modelObjectNode.toString());
                    modelData.setName(processName);
                    modelData.setKey(processName);

                    repositoryService.saveModel(modelData);

                    BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
                    ObjectNode editorNode = jsonConverter.convertToJson(bpmnModel);

                    repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            System.out.println("err4");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("err5");
                }
            }
        }
        return Result.OK("上传模型成功");
    }


    /**
     * 删除模型
     *
     * @param id 模型id
     * @return 是否删除成功
     */
    @AutoLog(value = "删除模型")
    @ApiOperation(value = "删除模型", notes = "根据模型标识删除模型")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public @ResponseBody
    Result<Object> deleteModel(@PathVariable("id") String id) {
        repositoryService.deleteModel(id);
        return Result.OK("删除成功。");
    }

    /**
     * 发布模型
     *
     * @param id 模型id
     * @return 是否部署成功
     */
    @AutoLog(value = "发布模型")
    @ApiOperation(value = "发布模型", notes = "发布模型为流程定义")
    @RequestMapping(value = "/deployment/{id}", method = RequestMethod.GET)
    public @ResponseBody
    Result<Object> deploy(@PathVariable("id") String id) {

        // 获取模型
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return Result.error("模型数据为空，请先成功设计流程并保存。然后再发布");
        }

        try {
            JsonNode modelNode = new ObjectMapper().readTree(bytes);

            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            if (model.getProcesses().size() == 0) {
                return Result.error("模型不符要求，请至少设计一条主线流程");
            }
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

            // 部署发布模型流程
            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .addString(processName, new String(bpmnBytes, StandardCharsets.UTF_8))
                    .deploy();

            //可以更新 model 中的 deployment id 字段 （看看后续有无必要）
            modelData.setDeploymentId(deployment.getId());
            repositoryService.saveModel(modelData);

            String metaInfo = modelData.getMetaInfo();
            JSONObject metaInfoMap = JSON.parseObject(metaInfo);
            // 设置流程分类 保存扩展流程至数据库
            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
            for (ProcessDefinition pd : list) {
                ActZprocess actZprocess = new ActZprocess();
                actZprocess.setId(pd.getId());
                actZprocess.setName(modelData.getName());
                actZprocess.setProcessKey(modelData.getKey());
                actZprocess.setDeploymentId(deployment.getId());
                actZprocess.setDescription(metaInfoMap.getString(ModelDataJsonConstants.MODEL_DESCRIPTION));
                actZprocess.setVersion(pd.getVersion());
                actZprocess.setDiagramName(pd.getDiagramResourceName());
                actZprocessService.setAllOldByProcessKey(modelData.getKey());
                actZprocess.setLatest(true);
                actZprocessService.save(actZprocess);
            }
        } catch (Exception e) {
            String err = e.toString();
            log.error(e.getMessage(), e);
            if (err.contains(NC_NAME)) {
                return Result.error("部署失败：流程设计中的流程名称不能为空，不能为数字以及特殊字符开头！");
            }
            if (err.contains(PRIMARY)) {
                return Result.error("部署失败：该模型已发布，key唯一！");
            }
            return Result.error("部署失败！");
        }

        return Result.OK("部署成功");
    }

    /**
     * 获取模型文件
     */
    @AutoLog(value = "获取模型文件")
    @ApiOperation(value = "获取模型文件", notes = "根据标识分别获取流程模型xml文件或者json文件")
    @RequestMapping(value = "/activiti/export/{modelId}/{type}", method = RequestMethod.GET)
    @ResponseBody
    public void export(@PathVariable("modelId") String modelId,
                       @PathVariable("type") String type,
                       HttpServletResponse response) throws IOException {
        try {
            Model modelData = repositoryService.getModel(modelId);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            byte[] modelEditorSource = repositoryService.getModelEditorSource(modelData.getId());
            JsonNode editorNode = new ObjectMapper().readTree(modelEditorSource);
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

            // 处理异常
            if (bpmnModel.getMainProcess() == null) {
                response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
                response.getOutputStream().println("没有主流程, 不能导出文件类型: " + type);
                response.flushBuffer();
                return;
            }

            String filename = "";
            byte[] exportBytes = null;
            String mainProcessId = bpmnModel.getMainProcess().getId();
            if (type.equals("bpmn")) {
                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                exportBytes = xmlConverter.convertToXML(bpmnModel);
                filename = mainProcessId + ".bpmn20.xml";
            }else if (type.equals("json")) {
                exportBytes = modelEditorSource;
                filename = mainProcessId + ".json";
            }

            // 处理异常
            if (exportBytes == null) {
                response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
                response.getOutputStream().println("模型内容空, 不能导出文件类型: " + type);
                response.flushBuffer();
                return;
            }

            ByteArrayInputStream in = new ByteArrayInputStream(exportBytes);
            response.setHeader("Content-Disposition", "attachment; filename=" +new String(filename.getBytes("gb2312"), "ISO8859-1"));
            IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.info("导出失败：modelId=" + modelId, e);
        }
    }

    /**
     * 获取模型图片
     */
    @AutoLog(value = "获取模型图片")
    @ApiOperation(value = "获取模型图片", notes = "获取模型的图片")
    @RequestMapping(value = "/activiti/exportDiagram", method = RequestMethod.GET)
    public void showModelPicture(String modelId, HttpServletResponse response) throws Exception {
        Model modelData = repositoryService.getModel(modelId);
        ObjectNode modelNode = null;
        try {
            modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        ProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();
        InputStream inputStream = processDiagramGenerator.generatePngDiagram(model);

        String filename = model.getMainProcess().getId() + ".png";
//        response.setHeader("Content-Disposition", "attachment; filename=" +new String(filename.getBytes("gb2312"), "ISO8859-1"));
        response.setHeader("Content-Disposition", "inline; filename=" +new String(filename.getBytes("gb2312"), "ISO8859-1"));

        OutputStream out = response.getOutputStream();
        for (int b; (b = inputStream.read()) != -1; ) {
            out.write(b);
        }
        out.close();
        inputStream.close();
    }

    /**
     * 获取流程定义图片
     */
    @AutoLog(value = "获取流程定义图片")
    @ApiOperation(value = "获取流程定义图片", notes = "导出部署流程资源, 用于流程定义中显示图片")
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportResource(@RequestParam String id, HttpServletResponse response) {

        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id).singleResult();

        String resourceName = pd.getDiagramResourceName();
        InputStream inputStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), resourceName);

        try {
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(resourceName, "UTF-8"));
            byte[] b = new byte[1024];
            int len;
            while ((len = inputStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
            response.flushBuffer();
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    /**
     * 获取实时高亮图片
     *
     * @param id       流程标识
     * @param response http响应
     */
    @AutoLog(value = "获取流程高亮图片")
    @ApiOperation(value = "获取流程高亮图片", notes = "获取流程高亮图片")
    @RequestMapping(value = "/getHighlightImg/{id}", method = RequestMethod.GET)
    public void getHighlightImg(@PathVariable String id, HttpServletResponse response) {
        InputStream inputStream;
        ProcessInstance pi;
        String picName;
        // 查询历史
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(id).singleResult();
        if (hpi.getEndTime() != null) {
            // 已经结束流程获取原图
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(hpi.getProcessDefinitionId()).singleResult();
            picName = pd.getDiagramResourceName();
            inputStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getDiagramResourceName());
        } else {
            pi = runtimeService.createProcessInstanceQuery().processInstanceId(id).singleResult();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());

            List<String> highLightedActivities = new ArrayList<>();
            // 高亮任务节点
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(id).list();
            for (Task task : tasks) {
                highLightedActivities.add(task.getTaskDefinitionKey());
            }

            List<String> highLightedFlows = new ArrayList<>();
            ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
            //"宋体"
            inputStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivities, highLightedFlows,
                    "宋体", "宋体", "宋体", null, 1.0);
            picName = pi.getName() + ".png";
        }
        try {
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(picName, "UTF-8"));
            byte[] b = new byte[1024];
            int len;
            while ((len = inputStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
            response.flushBuffer();
        } catch (IOException e) {
            log.error(e.toString());
            throw new JeecgBootException("读取流程图片失败");
        }
    }
}

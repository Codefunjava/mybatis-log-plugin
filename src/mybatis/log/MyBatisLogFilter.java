package mybatis.log;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import mybatis.log.hibernate.StringHelper;
import mybatis.log.util.PrintUtil;
import mybatis.log.util.RestoreSqlUtil;
import mybatis.log.util.StringConst;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * 语句过滤器
 * @author ob
 */
public class MyBatisLogFilter implements Filter {
    private final Project project;
    private static String preparingLine = "";
    private static String parametersLine = "";
    private static boolean isEnd = false;

    public MyBatisLogFilter(Project project) {
        this.project = project;
    }

    @Nullable
    @Override
    public Result applyFilter(final String currentLine, int endPoint) {
        ConfigVo configVo = MyBatisLogConfig.getConfigVo(project);
        if(configVo.getRunning()) {
            //过滤不显示的语句
            String[] filters = MyBatisLogConfig.properties.getValues(StringConst.FILTER_KEY);
            if (filters != null && filters.length > 0 && StringUtils.isNotBlank(currentLine)) {
                for (String filter : filters) {
                    if(StringUtils.isNotBlank(filter) && currentLine.toLowerCase().contains(filter.trim().toLowerCase())) {
                        return null;
                    }
                }
            }
            if(currentLine.contains(StringConst.PREPARING)) {
                preparingLine = currentLine;
                return null;
            }
            if(StringHelper.isEmpty(preparingLine)) {
                return null;
            }
            parametersLine = currentLine.contains(StringConst.PARAMETERS) ? currentLine : parametersLine + currentLine;
            if(!parametersLine.endsWith("Parameters: \n") && !parametersLine.endsWith("null\n") && !parametersLine.endsWith(")\n")) {
                return null;
            } else {
                isEnd = true;
            }
            if(StringHelper.isNotEmpty(preparingLine) && StringHelper.isNotEmpty(parametersLine) && isEnd) {
                String preStr = configVo.getIndexNum() + "  " + parametersLine.split(StringConst.PARAMETERS)[0].trim();//序号前缀字符串
                configVo.setIndexNum(configVo.getIndexNum() + 1);
                String restoreSql = RestoreSqlUtil.restoreSql(preparingLine, parametersLine);
                PrintUtil.println(project, preStr, ConsoleViewContentType.USER_INPUT);
                if(configVo.getSqlFormat()) {
                    restoreSql = PrintUtil.format(restoreSql);
                }
                PrintUtil.println(project, restoreSql);
                PrintUtil.println(project, StringConst.SPLIT_LINE, ConsoleViewContentType.USER_INPUT);
                preparingLine = "";
                parametersLine = "";
                isEnd = false;
            }
        }
        return null;
    }
}

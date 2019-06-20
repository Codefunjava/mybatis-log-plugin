package mybatis.log.action.gui;

import mybatis.log.hibernate.StringHelper;
import mybatis.log.util.RestoreSqlUtil;
import mybatis.log.util.StringConst;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author ob
 * @email huanglingbin@chainfly.com
 * @date 2019/6/20
 */
public class SqlText extends JFrame {
    private static String preparingLine = "";
    private static String parametersLine = "";
    private static boolean isEnd = false;

    private JPanel panel1;
    private JButton buttonOK;
    private JButton buttonClose;
    private JTextArea originalTextArea;
    private JTextArea resultTextArea;

    public SqlText() {
        this.setTitle("restore sql from text"); //设置标题
        setContentPane(panel1);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonClose.addActionListener(e -> onClose());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });
        panel1.registerKeyboardAction(e -> onClose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if(originalTextArea == null || StringUtils.isBlank(originalTextArea.getText())) {
            this.resultTextArea.setText("Can't restore sql from text.");
            return;
        }
        String originalText = originalTextArea.getText();
        if(originalText.contains(StringConst.PARAMETERS) && (originalText.contains(StringConst.PREPARING) || originalText.contains(StringConst.EXECUTING))) {
            String[] sqlArr = originalText.split("\n");
            if(sqlArr != null && sqlArr.length >= 2) {
                String resultSql = "";
                for(int i=0; i<sqlArr.length; ++i) {
                    String currentLine = sqlArr[i];
                    if(StringUtils.isBlank(currentLine)) {
                        continue;
                    }
                    if(currentLine.contains(StringConst.PREPARING) || currentLine.contains(StringConst.EXECUTING)) {
                        preparingLine = currentLine;
                        continue;
                    } else {
                        currentLine += "\n";
                    }
                    if(StringHelper.isEmpty(preparingLine)) {
                        continue;
                    }
                    if(currentLine.contains(StringConst.PARAMETERS)) {
                        parametersLine = currentLine;
                    } else {
                        if(org.apache.commons.lang.StringUtils.isBlank(parametersLine)) {
                            continue;
                        }
                        parametersLine += currentLine;
                    }
                    if(!parametersLine.endsWith("Parameters: \n") && !parametersLine.endsWith("null\n") && !parametersLine.endsWith(")\n")) {
                        if(i == sqlArr.length -1) {
                            this.resultTextArea.setText("Can't restore sql from text.");
                            break;
                        }
                        continue;
                    } else {
                        isEnd = true;
                    }
                    if(StringHelper.isNotEmpty(preparingLine) && StringHelper.isNotEmpty(parametersLine) && isEnd) {
                        resultSql += RestoreSqlUtil.restoreSql(preparingLine, parametersLine)
                                + "\n------------------------------------------------------------\n";
                    }
                }
                if(StringHelper.isNotEmpty(resultSql)) {
                    this.resultTextArea.setText(resultSql);
                }
            } else {
                this.resultTextArea.setText("Can't restore sql from text.");
            }
        } else {
            this.resultTextArea.setText("Can't restore sql from text.");
        }
    }

    private void onClose() {
        this.setVisible(false);
    }

    public static void main(String[] args) {
        SqlText dialog = new SqlText();
        dialog.pack();
        dialog.setSize(600, 320);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        System.exit(0);
    }
}

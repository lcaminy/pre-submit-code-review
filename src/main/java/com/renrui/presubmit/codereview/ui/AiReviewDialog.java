package com.renrui.presubmit.codereview.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.renrui.presubmit.codereview.model.Issue;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AiReviewDialog extends DialogWrapper {
    private static final Logger LOG = Logger.getInstance(AiReviewDialog.class);
    private final List<Issue> issues;
    private boolean okClicked = false;

    public AiReviewDialog(Project project, List<Issue> issues) {
        super(project);
        this.issues = issues;
        setTitle("代码审查结果");
        setSize(800, 600);
        init();
        LOG.info("显示代码审查结果对话框，问题数: " + issues.size());
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建问题列表
        JList<Issue> issueList = new JList<>(new DefaultListModel<>());
        DefaultListModel<Issue> model = (DefaultListModel<Issue>) issueList.getModel();
        for (Issue issue : issues) {
            model.addElement(issue);
            LOG.debug("添加问题到列表: " + issue.getType() + " - " + issue.getMessage());
        }

        // 设置单元格渲染器
        issueList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                         boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Issue) {
                    Issue issue = (Issue) value;
                    setText(String.format("[%s] %s (%s)", 
                        issue.getType().getDescription(),
                        issue.getMessage(),
                        issue.getFile()));
                }
                return this;
            }
        });

        // 创建详情面板
        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);

        // 添加选择监听器
        issueList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Issue selected = issueList.getSelectedValue();
                if (selected != null) {
                    detailArea.setText(String.format("类型: %s\n文件: %s\n行号: %d\n\n问题: %s\n\n建议: %s",
                        selected.getType().getDescription(),
                        selected.getFile(),
                        selected.getLine(),
                        selected.getMessage(),
                        selected.getSuggestion()));
                    LOG.debug("显示问题详情: " + selected.getType() + " - " + selected.getMessage());
                }
            }
        });

        // 分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            new JBScrollPane(issueList),
            new JBScrollPane(detailArea));
        splitPane.setDividerLocation(300);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected JComponent createSouthPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton continueButton = new JButton("继续提交");
        continueButton.addActionListener(e -> {
            okClicked = true;
            close(OK_EXIT_CODE);
            LOG.info("用户选择继续提交");
        });

        JButton cancelButton = new JButton("取消提交");
        cancelButton.addActionListener(e -> {
            close(CANCEL_EXIT_CODE);
            LOG.info("用户选择取消提交");
        });

        panel.add(continueButton);
        panel.add(cancelButton);
        return panel;
    }

    public boolean isOK() {
        return okClicked;
    }
} 
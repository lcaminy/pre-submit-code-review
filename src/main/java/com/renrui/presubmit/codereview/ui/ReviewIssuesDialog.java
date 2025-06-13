package com.renrui.presubmit.codereview.ui;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.renrui.presubmit.codereview.model.Issue;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

/**
 * 代码审查问题展示对话框
 */
public class ReviewIssuesDialog extends JDialog {
    private final Project project;
    private final List<Issue> issues;
    private final JBTable issuesTable;
    private final JButton autoFixButton;

    public ReviewIssuesDialog(Project project, List<Issue> issues) {
        super((Frame) null, "AI 代码审查结果", true);
        this.project = project;
        this.issues = issues;

        // 创建表格模型
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"类型", "文件", "行号", "描述", "建议"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 填充数据
        for (Issue issue : issues) {
            model.addRow(new Object[]{
                    issue.getType().getDescription(),
                    issue.getFile(),
                    issue.getLine(),
                    issue.getMessage(),
                    issue.getSuggestion()
            });
        }

        // 创建表格
        issuesTable = new JBTable(model);
        issuesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        issuesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        issuesTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        issuesTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        issuesTable.getColumnModel().getColumn(3).setPreferredWidth(250);
        issuesTable.getColumnModel().getColumn(4).setPreferredWidth(250);

        // 添加双击事件
        issuesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    navigateToIssue(issuesTable.getSelectedRow());
                }
            }
        });

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        autoFixButton = new JButton("自动修复选中项");
        autoFixButton.addActionListener(e -> autoFixSelectedIssues());
        buttonPanel.add(autoFixButton);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        // 布局
        setLayout(new BorderLayout());
        add(new JBScrollPane(issuesTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 设置对话框大小
        setSize(900, 400);
        setLocationRelativeTo(null);
    }

    /**
     * 跳转到问题位置
     */
    private void navigateToIssue(int row) {
        if (row < 0 || row >= issues.size()) return;

        Issue issue = issues.get(row);
        VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(new File(issue.getFile()));
        if (file != null) {
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, issue.getLine() - 1, 0);
            Editor editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
            if (editor != null) {
                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                editor.getCaretModel().moveToLogicalPosition(
                        editor.offsetToLogicalPosition(editor.getDocument().getLineStartOffset(issue.getLine() - 1))
                );
            }
        }
    }

    /**
     * 自动修复选中的问题
     */
    private void autoFixSelectedIssues() {
        int[] selectedRows = issuesTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "请选择要修复的问题",
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // TODO: 实现自动修复逻辑
        JOptionPane.showMessageDialog(this,
                "自动修复功能正在开发中",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
    }
} 
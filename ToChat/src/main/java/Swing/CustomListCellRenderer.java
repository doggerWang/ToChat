package Swing;

import javax.swing.*;
import java.awt.*;

public class CustomListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        String text = value.toString();
        if (text.contains("进入聊天室") || text.contains("离开聊天室")) {
            label.setForeground(Color.RED); // 设置字体颜色为红色
            label.setFont(new Font(label.getFont().getName(), Font.ITALIC, 8));
        } else {
            label.setForeground(Color.BLACK); // 默认字体颜色为黑色
        }

        return label;
    }
}

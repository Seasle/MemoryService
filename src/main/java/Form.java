import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.util.List;

public class Form {
    public JPanel contentPane;
    private JTable table;

    private void createUIComponents() {
        Object[][] data = {};
        Object[] columns = { "Диск", "Объем", "Занято", "Свободно" };
        table = new JTable(data, columns);
    }

    public void fillTable(List<Object[]> data) {
        TableColumnModel columnModel = table.getColumnModel();
        int count = columnModel.getColumnCount();
        
        Object[] columns = new Object[count];
        for (int index = 0; index < count; index++) {
            columns[index] = columnModel.getColumn(index).getHeaderValue();
        }

        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        for (Object[] entry : data) {
            tableModel.addRow(entry);
        }

        table.setModel(tableModel);
    }
}
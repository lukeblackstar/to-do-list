import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class Main extends JFrame {
    private DefaultListModel<String> listModel;
    private JList<String> taskList;
    private JTextField taskInput;
    private static final String DATA_FILE = "tasks.txt";
    private JList<String> categoryList;
    private DefaultListModel<String> categoryModel;

    public Main() {
        setTitle("To-Do List Moderna");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("nimbusBase", new Color(0x1C2451));
            UIManager.put("nimbusBlueGrey", new Color(0x2D3362));
            UIManager.put("control", new Color(0x3E4373));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(0x1C2451));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(150, 0));
        sidebarPanel.setBackground(new Color(0x2D3362));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        categoryModel = new DefaultListModel<>();
        categoryModel.addElement("Todas");
        categoryModel.addElement("Trabalho");
        categoryModel.addElement("Pessoal");
        categoryModel.addElement("Compras");

        categoryList = new JList<>(categoryModel);
        categoryList.setBackground(new Color(0x3E4373));
        categoryList.setForeground(new Color(220, 220, 220));
        categoryList.setSelectionBackground(new Color(0x5F6195));
        categoryList.setSelectionForeground(Color.WHITE);
        categoryList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        categoryList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        categoryList.setFixedCellHeight(35);

        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        categoryScrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        sidebarPanel.add(categoryScrollPane, BorderLayout.CENTER);

        JButton addCategoryButton = new JButton("+ Categoria");
        addCategoryButton.setBackground(new Color(0x4E5284));
        addCategoryButton.setForeground(Color.WHITE);
        addCategoryButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addCategoryButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        addCategoryButton.setFocusPainted(false);
        addCategoryButton.addActionListener(e -> addNewCategory());
        sidebarPanel.add(addCategoryButton, BorderLayout.SOUTH);

        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setBackground(new Color(0x3E4373));
        taskList.setForeground(new Color(220, 220, 220));
        taskList.setSelectionBackground(new Color(0x5F6195));
        taskList.setSelectionForeground(Color.WHITE);
        taskList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        taskList.setFixedCellHeight(40);
        
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout(10, 0));
        inputPanel.setBackground(new Color(0x1C2451));

        taskInput = new JTextField();
        taskInput.setBackground(new Color(0x3E4373));
        taskInput.setForeground(Color.WHITE);
        taskInput.setCaretColor(Color.WHITE);
        taskInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        inputPanel.add(taskInput, BorderLayout.CENTER);

        JButton addButton = new JButton("+");
        addButton.setBackground(new Color(0x4E5284));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        addButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        addButton.setFocusPainted(false);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String task = taskInput.getText().trim();
                if (!task.isEmpty()) {
                    String selectedCategory = (String) JOptionPane.showInputDialog(
                        Main.this,
                        "Selecione a categoria:",
                        "Adicionar Tarefa",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        getCategoriesArray(),
                        "Todas"
                    );
                    
                    if (selectedCategory != null) {
                        addTaskWithCategory(task, selectedCategory);
                        taskInput.setText("");
                    }
                }
            }
        });
        inputPanel.add(addButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        controlPanel.setBackground(new Color(0x1C2451));

        JButton completeButton = createButton("Concluir", new Color(0x5F6195));
        completeButton.addActionListener(e -> markTaskComplete());
        controlPanel.add(completeButton);

        JButton removeButton = createButton("Remover", new Color(0x4E5284));
        removeButton.addActionListener(e -> removeTask());
        controlPanel.add(removeButton);

        mainPanel.add(controlPanel, BorderLayout.NORTH);

        add(mainPanel);

        loadTasks();

        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedCategory = categoryList.getSelectedValue();
                if (selectedCategory != null) {
                    filterTasksByCategory(selectedCategory);
                }
            }
        });
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void markTaskComplete() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            String task = listModel.getElementAt(selectedIndex);
            if (!task.contains("(Concluído)")) {
                listModel.set(selectedIndex, task + " (Concluído)");
                saveTasks();
            }
        }
    }

    private void removeTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            String task = taskList.getModel().getElementAt(selectedIndex);
            listModel.removeElement(task);
            saveTasks();
            String selectedCategory = categoryList.getSelectedValue();
            if (selectedCategory != null) {
                filterTasksByCategory(selectedCategory);
            }
        }
    }

    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (int i = 0; i < listModel.size(); i++) {
                writer.write(listModel.getElementAt(i));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    listModel.addElement(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addNewCategory() {
        String newCategory = JOptionPane.showInputDialog(this, "Digite o nome da nova categoria:");
        if (newCategory != null && !newCategory.trim().isEmpty()) {
            categoryModel.addElement(newCategory.trim());
        }
    }

    private String[] getCategoriesArray() {
        String[] categories = new String[categoryModel.size()];
        for (int i = 0; i < categoryModel.size(); i++) {
            categories[i] = categoryModel.getElementAt(i);
        }
        return categories;
    }

    private void addTaskWithCategory(String task, String category) {
        if (category.equals("Todas")) {
            listModel.addElement(task);
        } else {
            listModel.addElement("[" + category + "] " + task);
        }
        saveTasks();
    }

    private void filterTasksByCategory(String category) {
        DefaultListModel<String> filteredModel = new DefaultListModel<>();
        for (int i = 0; i < listModel.size(); i++) {
            String task = listModel.getElementAt(i);
            if (category.equals("Todas") || task.contains("[" + category + "]")) {
                filteredModel.addElement(task);
            }
        }
        taskList.setModel(filteredModel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
} 
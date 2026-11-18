package controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

import models.Sales;
import models.SalesDao;
import models.Products;
import models.ProductsDao;
import views.SystemView;
import javax.swing.table.DefaultTableModel;
import models.Customers;
import models.CustomersDao;

public class SalesController implements ActionListener, MouseListener, KeyListener {

    // Instanciar los modelos y la vista
    private Sales sale;
    private SalesDao saleDao;
    private SystemView views;

    public SalesController(Sales sale, SalesDao saleDao, SystemView views, DefaultTableModel model, DefaultTableModel temp) {
        this.sale = sale;
        this.saleDao = saleDao;
        this.views = views;
        this.model = model;
        this.temp = temp;

        // Eventos de los botones de ventas
        views.btn_add_product_sale.addActionListener(this);
        views.btn_confirm_sale.addActionListener(this);
        views.btn_new_sale.addActionListener(this);
        views.btn_remove_sale.addActionListener(this);
        views.jLabelSales.addMouseListener(this);
        views.jLabelReports.addMouseListener(this);

        // Evento para buscar productos por código
        views.txt_sale_product_code.addKeyListener(this);

        views.txt_sale_customer_id.addKeyListener(this);
        views.txt_sale_quantify.addKeyListener(this);

    }

    Products product = new Products();
    ProductsDao productDao = new ProductsDao();

    private int item = 0;
    String rol = models.EmployessDao.rol_user;

    private DefaultTableModel model;
    private DefaultTableModel temp;

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == views.btn_confirm_sale) {

            insertSale();

        } else if (e.getSource() == views.btn_new_sale) {

            cleanTableTemp();
            cleanAllFieldsSales();
            views.txt_sale_customer_id.requestFocus();

        } else if (e.getSource() == views.btn_remove_sale) {

            temp = (DefaultTableModel) views.sales_table.getModel();

            if (views.sales_table.getSelectedRow() != -1) {

                temp.removeRow(views.sales_table.getSelectedRow());

                calculateSales();

                if (views.sales_table.getRowCount() == 0) {
                    views.txt_sale_customer_id.setText("");
                    views.txt_sale_customer_name.setText("");
                    views.txt_sale_total_to_pay.setText("");
                }
            }

            views.txt_sale_product_code.requestFocus();

        } else if (e.getSource() == views.btn_add_product_sale) {

            if (!"".equals(views.txt_sale_quantify.getText())) {

                int amount = Integer.parseInt(views.txt_sale_quantify.getText());
                String product_name = views.txt_sale_product_name.getText();
                double price = Double.parseDouble(views.txt_sale_price.getText());
                int sale_id = Integer.parseInt(views.txt_sale_product_id.getText());
                double subtotal = amount * price;
                int stock = Integer.parseInt(views.txt_sale_stock.getText());
                String full_name = views.txt_sale_customer_name.getText();

                if (stock >= amount) {

                    item = item + 1;
                    temp = (DefaultTableModel) views.sales_table.getModel();

                    for (int i = 0; i < views.sales_table.getRowCount(); i++) {

                        if (views.sales_table.getValueAt(i, 1).equals(views.txt_sale_product_name.getText())) {
                            JOptionPane.showMessageDialog(null, "El producto ya esta registrado en la tabla de ventas");
                            return;
                        }
                    }

                    ArrayList list = new ArrayList();

                    list.add(item);
                    list.add(sale_id);
                    list.add(product_name);
                    list.add(amount);
                    list.add(price);
                    list.add(subtotal);
                    list.add(full_name);

                    Object[] obj = new Object[6];

                    obj[0] = list.get(1);
                    obj[1] = list.get(2);
                    obj[2] = list.get(3);
                    obj[3] = list.get(4);
                    obj[4] = list.get(5);
                    obj[5] = list.get(6);

                    temp.addRow(obj);
                    views.sales_table.setModel(temp);
                    calculateSales();

                    cleanFieldsSales();
                    views.txt_sale_product_code.requestFocus();

                } else {
                    JOptionPane.showMessageDialog(null, "Stock no disponible");
                }

            } else {
                JOptionPane.showMessageDialog(null, "Ingrese cantidad");
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        System.out.println("Se hizo clic en: " + e.getSource());

        if (e.getSource() == views.jLabelSales) {

            views.jTabbedPane1.setSelectedIndex(2);

        } else if (e.getSource() == views.jLabelReports) {

            if (rol.equals("Administrador")) {

                views.jTabbedPane1.setSelectedIndex(7);

                listAllSales();

            } else {

                views.jTabbedPane1.setEnabledAt(7, false);

                views.jLabelReports.setEnabled(false);

                JOptionPane.showMessageDialog(null, "No tiene privilegios de administrador para acceder a esta vista");

                JOptionPane.showMessageDialog(null, "¿El clic llegó a jLabelSales? " + (e.getSource() == views.jLabelSales));
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {

        // Buscar producto por código
        if (e.getSource() == views.txt_sale_product_code) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                if (!"".equals(views.txt_sale_product_code.getText())) {

                    int code = Integer.parseInt(views.txt_sale_product_code.getText());

                    product = productDao.searchCode(code);

                    if (product.getName() != null) {

                        views.txt_sale_product_name.setText(product.getName());
                        views.txt_sale_product_id.setText("" + product.getId());
                        views.txt_sale_stock.setText("" + product.getProduct_quantity());
                        views.txt_sale_price.setText("" + product.getUnit_price());

                        views.txt_sale_quantify.requestFocus();

                    } else {
                        JOptionPane.showMessageDialog(null, "No existe ningún producto con ese código");
                        cleanFieldsSales();
                        views.txt_sale_product_code.requestFocus();
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "Ingrese el código del producto a vender");
                }
            }

            // Buscar cliente por identificación
        } else if (e.getSource() == views.txt_sale_customer_id) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                Customers customer = new Customers();
                CustomersDao customerDao = new CustomersDao();

                if (!"".equals(views.txt_sale_customer_id.getText())) {

                    int customer_id = Integer.parseInt(views.txt_sale_customer_id.getText());

                    customer = customerDao.searchCustomer(customer_id);

                    if (customer.getFull_name() != null) {

                        views.txt_sale_customer_name.setText("" + customer.getFull_name());

                    } else {

                        views.txt_sale_customer_id.setText("");
                        JOptionPane.showMessageDialog(null, "El cliente no existe");

                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getSource() == views.txt_sale_quantify) {
            if (!"".equals(views.txt_sale_quantify.getText())) {
                int quantity = Integer.parseInt(views.txt_sale_quantify.getText());
                double price = Double.parseDouble(views.txt_sale_price.getText());
                views.txt_sale_subtotal.setText("" + quantity * price);
            } else {
                views.txt_sale_subtotal.setText("");
            }
        }
    }

    // Registrar venta
    private void insertSale() {

        if (views.sales_table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Agregue al menos un producto a la venta");
            return;
        }

        if (views.txt_sale_customer_id.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese el cliente");
            return;
        }

        if (views.txt_sale_total_to_pay.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se ha calculado el total de la venta");
            return;
        }

        int customer_id = Integer.parseInt(views.txt_sale_customer_id.getText());
        int employee_id = models.EmployessDao.id_user;
        double total = Double.parseDouble(views.txt_sale_total_to_pay.getText());

        if (saleDao.registerSaleQuery(customer_id, employee_id, total)) {
            int sale_id = saleDao.getMaxSaleIdQuery();

            for (int i = 0; i < views.sales_table.getRowCount(); i++) {
                int product_id = Integer.parseInt(views.sales_table.getValueAt(i, 0).toString());
                int sale_quantity = Integer.parseInt(views.sales_table.getValueAt(i, 2).toString());
                double sale_price = Double.parseDouble(views.sales_table.getValueAt(i, 3).toString());
                double sale_subtotal = Double.parseDouble(views.sales_table.getValueAt(i, 4).toString());

                saleDao.registerSaleDetailQuery(
                        product_id, sale_id, sale_quantity, sale_price, sale_subtotal
                );

                product = productDao.searchProduct(product_id);
                int stock = product.getProduct_quantity() - sale_quantity;
                productDao.updateStockQuery(stock, product_id);
            }
            listAllSales();

            JOptionPane.showMessageDialog(null, "Venta generada");
            cleanTableTemp();
            cleanFieldsSales();
        }
    }

// Listar todas las ventas
    public void listAllSales() {
        if (rol.equals("Administrador")) {

            List<Sales> list = saleDao.listAllSalesQuery();

            model = (DefaultTableModel) views.table_all_sales.getModel();

            // Limpiar registros anteriores de la tabla
            model.setRowCount(0);

            Object[] row = new Object[5];

            for (int i = 0; i < list.size(); i++) {
                row[0] = list.get(i).getId();
                row[1] = list.get(i).getCustomer_name();
                row[2] = list.get(i).getEmployee_name();
                row[3] = list.get(i).getTotal_to_pay();
                row[4] = list.get(i).getSale_date();

                model.addRow(row);
            }

            views.table_all_sales.setModel(model);
        }
    }

    // Limpiar tabla temporal de ventas
    public void cleanTableTemp() {

        for (int i = 0; i < temp.getRowCount(); i++) {
            temp.removeRow(i);
            i = i - 1;
        }
    }
    // Limpiar tabla

    public void cleanTable() {
        for (int i = 0; i < model.getRowCount(); i++) {
            model.removeRow(i);
            i = i - 1;
        }
    }

    // Limpiar campos de ventas
    private void cleanFieldsSales() {
        views.txt_sale_product_code.setText("");
        views.txt_sale_product_name.setText("");
        views.txt_sale_product_id.setText("");
        views.txt_sale_stock.setText("");
        views.txt_sale_price.setText("");
        views.txt_sale_quantify.setText("");
        views.txt_sale_subtotal.setText("");
        views.txt_sale_customer_id.setText("");
        views.txt_sale_customer_name.setText("");
    }

    // Limpiar todos los campos
    public void cleanAllFieldsSales() {
        views.txt_sale_product_code.setText("");
        views.txt_sale_product_name.setText("");
        views.txt_sale_quantify.setText("");
        views.txt_sale_product_id.setText("");
        views.txt_sale_price.setText("");
        views.txt_sale_subtotal.setText("");
        views.txt_sale_customer_id.setText("");
        views.txt_sale_customer_name.setText("");
        views.txt_sale_total_to_pay.setText("");
        views.txt_sale_stock.setText("");
    }

    /*Calcular total de ventas
    private void calculateSales() {
        double total = 0;

        for (int i = 0; i < views.sales_table.getRowCount(); i++) {
            total += Double.parseDouble(
                    views.sales_table.getValueAt(i, 4).toString()
            );
        }

        views.txt_sale_total_to_pay.setText(String.valueOf(total));
        System.out.println("TOTAL CALCULADO: " + total);
    }*/
    private void calculateSales() {
        double total = 0;

        for (int i = 0; i < views.sales_table.getRowCount(); i++) {
            total += Double.parseDouble(
                    views.sales_table.getValueAt(i, 4).toString()
            );
        }

        views.txt_sale_total_to_pay.setText(String.valueOf(total));
        views.txt_sale_total_to_pay.setForeground(java.awt.Color.BLACK);

        System.out.println("TOTAL CALCULADO: " + total);
        System.out.println("CAMPO TOTAL: " + views.txt_sale_total_to_pay.getText());
        System.out.println("VISIBLE: " + views.txt_sale_total_to_pay.isVisible());
    }

}

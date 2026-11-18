package controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import models.DynamicCombobox;
import static models.EmployessDao.id_user;
import static models.EmployessDao.rol_user;
import models.Products;
import models.ProductsDao;
import models.Purchases;
import models.PurchasesDao;
import views.Print;
import views.SystemView;

public class PurchasesController implements KeyListener, ActionListener, MouseListener {

    private Purchases purchase;
    private PurchasesDao purchaseDao;
    private SystemView views;

    private int getIdSupplier = 0;
    private int item = 0;

    DefaultTableModel model = new DefaultTableModel();
    DefaultTableModel temp;

    // Instanciar el modelo productos
    Products product = new Products();
    ProductsDao productDao = new ProductsDao();

    String rol = rol_user;

    // Constructor
    public PurchasesController(Purchases purchase, PurchasesDao purchaseDao, SystemView views) {

        this.purchase = purchase;
        this.purchaseDao = purchaseDao;
        this.views = views;

        // Botón de agregar producto
        this.views.btn_add_product_to_buy.addActionListener(this);

        // Botón de confirmar compra
        this.views.btn_confirm_purchase.addActionListener(this);

        //Botón de eliminar compra 
        this.views.btn_remove_purchase.addActionListener(this);

        // Eventos de teclado
        this.views.txt_purchase_product_code.addKeyListener(this);
        this.views.txt_purchase_price.addKeyListener(this);

        this.views.btn_new_purchase.addActionListener(this);
        this.views.jLabelPurchases.addMouseListener(this);
        this.views.jLabelReports.addMouseListener(this);

    }

    // Eventos de botones
    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == views.btn_add_product_to_buy) {

            DynamicCombobox supplier_cmb
                    = (DynamicCombobox) views.cmb_purchase_supplier.getSelectedItem();

            if (supplier_cmb == null) {
                JOptionPane.showMessageDialog(null, "Selecciona un proveedor");
                return;
            }

            int supplier_id = supplier_cmb.getId();

            if (getIdSupplier == 0) {
                getIdSupplier = supplier_id;

            } else {

                if (getIdSupplier != supplier_id) {

                    JOptionPane.showMessageDialog(null,
                            "No puede realizar una misma compra a varios proveedores");

                } else {

                    // Validar campos
                    if (views.txt_purchase_amount.getText().isEmpty()
                            || views.txt_purchase_price.getText().isEmpty()
                            || views.txt_purchase_id.getText().isEmpty()
                            || views.txt_purchase_product_name.getText().isEmpty()) {

                        JOptionPane.showMessageDialog(null,
                                "Completa todos los campos del producto");
                        return;
                    }

                    try {

                        int amount = Integer.parseInt(
                                views.txt_purchase_amount.getText());

                        String product_name
                                = views.txt_purchase_product_name.getText();

                        double price = Double.parseDouble(
                                views.txt_purchase_price.getText());

                        int purchase_id = Integer.parseInt(
                                views.txt_purchase_id.getText());

                        String supplier_name
                                = views.cmb_purchase_supplier.getSelectedItem().toString();

                        if (amount <= 0 || price <= 0) {
                            JOptionPane.showMessageDialog(null,
                                    "La cantidad y el precio deben ser mayores a cero");
                            return;
                        }

                        // Obtener modelo de la tabla
                        temp = (DefaultTableModel) views.purchases_table.getModel();

                        // Verificar productos duplicados
                        for (int i = 0; i < views.purchases_table.getRowCount(); i++) {

                            if (views.purchases_table.getValueAt(i, 1)
                                    .equals(product_name)) {

                                JOptionPane.showMessageDialog(null,
                                        "El producto ya esta registrado en la tabla de compras");
                                return;
                            }
                        }

                        // Agregar datos del producto
                        ArrayList<Object> list = new ArrayList<>();

                        item = 1;
                        list.add(item);
                        list.add(purchase_id);
                        list.add(product_name);
                        list.add(amount);
                        list.add(price);
                        list.add(amount * price);
                        list.add(supplier_name);

                        Object[] obj = new Object[6];

                        obj[0] = list.get(1);
                        obj[1] = list.get(2);
                        obj[2] = list.get(3);
                        obj[3] = list.get(4);
                        obj[4] = list.get(5);
                        obj[5] = list.get(6);

                        temp.addRow(obj);

                        views.purchases_table.setModel(temp);

                        // Limpiar campos
                        cleanFieldsPurchases();

                        views.cmb_purchase_supplier.setEditable(false);

                        views.txt_purchase_product_code.requestFocus();

                        // Calcular total
                        calculatePurchase();

                    } catch (NumberFormatException ex) {

                        JOptionPane.showMessageDialog(null,
                                "Verifica que la cantidad y el precio sean números válidos");
                    }
                }
            }

        } else if (e.getSource() == views.btn_confirm_purchase) {
            insertPurchase();

        } else if (e.getSource() == views.btn_remove_purchase) {

            int fila = views.purchases_table.getSelectedRow();

            if (fila == -1) {
                JOptionPane.showMessageDialog(null,
                        "Selecciona un producto de la tabla para eliminar");
            } else {

                int respuesta = JOptionPane.showConfirmDialog(
                        null,
                        "¿Deseas eliminar este producto de la compra?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION
                );

                if (respuesta == JOptionPane.YES_OPTION) {

                    int filaModelo = views.purchases_table.convertRowIndexToModel(fila);

                    DefaultTableModel tabla
                            = (DefaultTableModel) views.purchases_table.getModel();

                    tabla.removeRow(filaModelo);

                    calculatePurchase();

                    if (tabla.getRowCount() == 0) {
                        getIdSupplier = 0;
                        views.cmb_purchase_supplier.setEditable(true);
                    }
                }
            }
        }else if (e.getSource() == views.btn_new_purchase) {
            cleanTableTemp();
            cleanFieldsPurchases();

            getIdSupplier = 0;

            views.cmb_purchase_supplier.setEditable(true);

        }
    }

    // Registrar compra
    private void insertPurchase() {

        if (getIdSupplier == 0 || views.purchases_table.getRowCount() == 0) {

            JOptionPane.showMessageDialog(null,
                    "Agrega productos y selecciona un proveedor");
            return;
        }

        if (views.txt_purchase_total_to_pay.getText().isEmpty()) {

            JOptionPane.showMessageDialog(null,
                    "No hay un total para registrar");
            return;
        }

        try {

            double total = Double.parseDouble(
                    views.txt_purchase_total_to_pay.getText());

            int employee_id = id_user;

            // Registrar compra
            if (purchaseDao.registerPurchaseQuery(
                    getIdSupplier, employee_id, total)) {

                int purchase_id = purchaseDao.purchaseId();

                // Registrar detalles de la compra
                for (int i = 0; i < views.purchases_table.getRowCount(); i++) {

                    int product_id = Integer.parseInt(
                            views.purchases_table.getValueAt(i, 0).toString());

                    int purchase_amount = Integer.parseInt(
                            views.purchases_table.getValueAt(i, 2).toString());

                    double purchase_price = Double.parseDouble(
                            views.purchases_table.getValueAt(i, 3).toString());

                    double purchase_subtotal
                            = purchase_price * purchase_amount;

                    purchaseDao.registerPurchaseDetailQuery(
                            purchase_id,
                            purchase_price,
                            purchase_amount,
                            purchase_subtotal,
                            product_id);
                    productDao.updateStockQuery(purchase_amount, product_id);

                    //Traer la cantidad de productos
                    product = productDao.searchId(product_id);
                    int amount = product.getProduct_quantity() + purchase_amount;

                }

                // Limpiar tabla temporal
                cleanTableTemp();

                // Limpiar campos
                cleanFieldsPurchases();

                // Reiniciar proveedor seleccionado
                getIdSupplier = 0;

                views.cmb_purchase_supplier.setEditable(true);

                JOptionPane.showMessageDialog(null, "Compra generada con exito");
                Print print = new Print(purchase_id);
                print.setVisible(true);

            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(null, "El total de la compra no es válido");
        }
    }

    //Método para listar las compras realizadas
    public void listAllPurchases () {
        if(rol.equals ("Administrador") || rol.equals ("Auxiliar") ) {
            List<Purchases> list = purchaseDao.listAllPurchasesQuery();
            model = (DefaultTableModel) views.table_all_purchases.getModel();
            Object [] row = new Object [4];
            //Recorrer con ciclo for
            for(int i = 0; i<list.size(); i++){
                row[0] = list.get(i).getId();
                row[1] = list.get(i) .getSupplier_name_product ();
                row[2] = list.get(i).getTotal ();
                row[3] = list.get(i).getCreated();
                model.addRow(row);
            }
            views. table_all_purchases. setModel (model);
        }
    }

    // Detectar teclas escritas
    @Override
    public void keyTyped(KeyEvent e) {
    }

    // Buscar producto por código al presionar ENTER
    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getSource() == views.txt_purchase_product_code) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                String code = views.txt_purchase_product_code.getText().trim();

                if (code.isEmpty()) {

                    JOptionPane.showMessageDialog(null, "Ingresa el codigo del producto a comprar");

                } else {

                    try {

                        int id = Integer.parseInt(code);

                        product = productDao.searchCode(id);

                        // Verificar si existe el producto
                        if (product.getId() == 0) {

                            JOptionPane.showMessageDialog(null,"Producto no encontrado");

                            views.txt_purchase_product_name.setText("");
                            views.txt_purchase_id.setText("");

                            views.txt_purchase_product_code.requestFocus();

                        } else {

                            views.txt_purchase_product_name.setText(
                                    product.getName());

                            views.txt_purchase_id.setText(
                                    "" + product.getId());

                            views.txt_purchase_amount.requestFocus();
                        }

                    } catch (NumberFormatException ex) {

                        JOptionPane.showMessageDialog(null,
                                "Ingresa un código numérico válido");
                    }
                }
            }
        }
    }

    // Calcular subtotal al escribir el precio
    @Override
    public void keyReleased(KeyEvent e) {

        if (e.getSource() == views.txt_purchase_price) {

            String amountText = views.txt_purchase_amount.getText().trim();
            String priceText = views.txt_purchase_price.getText().trim();

            if (amountText.isEmpty() || priceText.isEmpty()) {

                views.txt_purchase_subtotal.setText("");

            } else {

                try {

                    int quantity = Integer.parseInt(amountText);
                    double price = Double.parseDouble(priceText);

                    views.txt_purchase_subtotal.setText(
                            "" + quantity * price);

                } catch (NumberFormatException ex) {

                    views.txt_purchase_subtotal.setText("");
                }
            }
        }
    }

    // Limpiar campos
    public void cleanFieldsPurchases() {

        views.txt_purchase_product_name.setText("");
        views.txt_purchase_price.setText("");
        views.txt_purchase_amount.setText("");
        views.txt_purchase_product_code.setText("");
        views.txt_purchase_subtotal.setText("");
        views.txt_purchase_id.setText("");
        views.txt_purchase_total_to_pay.setText("");
    }

    // Calcular total a pagar
    public void calculatePurchase() {

        double total = 0.00;

        int numRow = views.purchases_table.getRowCount();

        for (int i = 0; i < numRow; i++) {

            // Sumar la columna de subtotales
            total = total + Double.parseDouble(
                    String.valueOf(
                            views.purchases_table.getValueAt(i, 4)));
        }

        views.txt_purchase_total_to_pay.setText("" + total);
    }

    // Limpiar tabla temporal
    public void cleanTableTemp() {

        temp = (DefaultTableModel) views.purchases_table.getModel();

        for (int i = 0; i < temp.getRowCount(); i++) {

            temp.removeRow(i);
            i = i - 1;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getSource() == views.jLabelPurchases) {
            if(rol.equals("Administrador")) {
                views.jTabbedPane1.setSelectedIndex (1) ;
                cleanTable();
            }else{
                views.jTabbedPane1.setEnabledAt (1, false);
                views.jLabelPurchases.setEnabled (false) ;
                JOptionPane. showMessageDialog(null, "No tiene privilegios de administrador para ingresar a esta vista");
            }
        }else if (e.getSource () == views. jLabelReports) {
            views.jTabbedPane1.setSelectedIndex (7) ;
            cleanTable();
            listAllPurchases ();
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

    //Limpiar tabla
    public void cleanTable () {
        for (int i = 0; i < model.getRowCount (); i++) {
            model.removeRow(i);
            i=i -1;
        }
    }
}

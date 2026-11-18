package controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import models.Employees;
import models.EmployessDao;
import views.LoginView;
import views.SystemView;

public class LoginController implements ActionListener {

    // Variables
    private Employees employee;
    private EmployessDao employees_dao;
    private LoginView login_view;

    // Constructor
    public LoginController(Employees employee,
            EmployessDao employees_dao, LoginView login_view) {

        this.employee = employee;
        this.employees_dao = employees_dao;
        this.login_view = login_view;

        this.login_view.btn_enter.addActionListener(this);
    }

    // Acción del botón
    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == login_view.btn_enter) {

            // Obtener los datos de la vista
            String user = login_view.txt_username.getText().trim();
            String pass = String.valueOf(
                    login_view.txt_password.getPassword());

            // Validar que los campos no estén vacíos
            if (!user.equals("") && !pass.equals("")) {

                // Validar el usuario
                employee = employees_dao.loginQuery(user, pass);

                if (employee != null && employee.getUsername() != null) {

                    // Verificar el rol del usuario
                    if (employee.getRol().equals("Administrador")) {
                        SystemView admin = new SystemView();
                        admin.setVisible(true);
                    } else {
                        SystemView aux = new SystemView();
                        aux.setVisible(true);
                    }

                    this.login_view.dispose();

                } else {
                    JOptionPane.showMessageDialog(
                            null, "Usuario o contraseña incorrectos");
                }

            } else {
                JOptionPane.showMessageDialog(
                        null, "Los campos están vacíos");
            }
        }
    }
}
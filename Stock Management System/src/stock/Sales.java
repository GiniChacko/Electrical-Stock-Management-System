

package stock;


import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

/**
 * 
 * @author Candida , Lizel , Gini
 */
public class Sales extends javax.swing.JFrame {

    /**
     * Creates new form sales
     */
    public Sales() {
        initComponents();
         Connect();
    }
    
    
    
    
    
    
    
    
    Connection con;
    PreparedStatement pst;
    PreparedStatement pst1;
    PreparedStatement pst2;
    DefaultTableModel df;
    ResultSet rs;
    
    public void Connect()
    {
        try {
            Class.forName("com.mysql.jdbc.Driver");
             con =  DriverManager.getConnection("jdbc:mysql://localhost/stockmanagement","root","");
            
  
        } 
        catch (ClassNotFoundException ex)
        {
            Logger.getLogger(Vendor.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(SQLException ex)
        {
             Logger.getLogger(Vendor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    
    public void barcode()
    {
      try{
       
          String pcode = txtpcode.getText();
        
          pst = con.prepareStatement("select * from product where barcode = ?");
          pst.setString(1, pcode);
          rs = pst.executeQuery();
          
          if(rs.next() == false)
          {
              JOptionPane.showMessageDialog(this, "BarCode Not Found");
              txtpcode.setText("");
          }
          else
          {
              String pname = rs.getString("pname");
              String price = rs.getString("rprice");
              
              txtpname.setText(pname.trim());
              txtprice.setText(price.trim());
              txtqty.requestFocus();
          }
          
          
          
          
    }catch(SQLException ex){
        Logger.getLogger(Purchase.class.getName()).log(Level.SEVERE, null , ex );
    }
      
      
    }
    
    
    
    public void Sales() // checks if  the qyantity is enough before selling the product
    {
        try 
        {
            String pcode = txtpcode.getText();

            
             pst =  con.prepareStatement("select * from product where barcode  = ?");
            pst.setString(1,pcode);
            rs = pst.executeQuery();
            
            while(rs.next())
            {
                int currentqty;
                currentqty = rs.getInt("qty");
                
                int price = Integer.parseInt(txtprice.getText());
                int qty = Integer.parseInt(txtqty.getText());
                int tot = price * qty;    
                
                if(qty >= currentqty)
                {
                    JOptionPane.showMessageDialog(this,"Quantity Not Enough"); 
                }
                else 
                {
                    df = (DefaultTableModel)jTable1.getModel();
                    df.addRow(new Object[]       
                    {
                    
                        txtpcode.getText(),
                        txtpname.getText(),
                        txtprice.getText(),
                        txtqty.getText(),
                        tot                            
                    });
                 int sum =0;
                    
                    for(int i=0; i<jTable1.getRowCount();i++)
                    {
                        sum = sum + Integer.parseInt(jTable1.getValueAt(i, 4).toString());
                    }
                    
                    txttcost.setText(String.valueOf(sum));
                    
                    
                    txtpcode.setText("");
                    txtpname.setText("");
                    txtprice.setText("");        
                    txtqty.setText("");
                }   
                    
            }
        }
                    
        catch (SQLException ex) 
        {
            Logger.getLogger(Sales.class.getName()).log(Level.SEVERE, null, ex);
        }
          
      }           
                    
                    
                    
                    
                    
                    
                    
                   
               
   
        
        
        
        
        public void add() throws FileNotFoundException
      {
          
          try{
          DateTimeFormatter dt = DateTimeFormatter.ofPattern("YYYY/MM/dd");
          LocalDateTime now = LocalDateTime.now();
          String date = dt.format(now);
          
             //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            //LocalDateTime now = LocalDateTime.now();
             //String date = dtf.format(now); //2016/11/16 12:08:43
          
         
          String subtotal = txttcost.getText();
          String pay = txtpay.getText();
          String bal = txtbal.getText();
          int lastid = 0;
          
          String query1 = "insert into sales(date,subtotal,pay,bal)values(?,?,?,?)"; //query
          pst = con.prepareStatement(query1,Statement.RETURN_GENERATED_KEYS);
          
          pst.setString(1, date);
          pst.setString(2, subtotal);
          pst.setString(3, pay);
          pst.setString(4, bal);
          pst.executeUpdate(); //fire query on data base (return no of columns affected , data manipulation query)
          rs = pst.getGeneratedKeys();
          
          if(rs.next())
          {
              lastid = rs.getInt(1);
          }
          
          String query2 = "insert into sales_product(sales_id,pid,price,qty,total)values(?,?,?,?,?)";
          pst1 = con.prepareStatement(query2);
          
          String productid;
          String price;
          String qty;
          int total = 0;
          
          for(int i=0; i<jTable1.getRowCount(); i++)
          {
              productid = (String)jTable1.getValueAt(i, 0);
              price = (String)jTable1.getValueAt(i, 2);
              qty = (String)jTable1.getValueAt(i, 3);
              total = (int)jTable1.getValueAt(i, 4);
              
              pst1.setInt(1, lastid);
              pst1.setString(2, productid);
              pst1.setString(3, price);
              pst1.setString(4, qty);
              pst1.setInt(5, total);
              pst1.executeUpdate();
          }
          
          String query3 = "update product set qty = qty- ? where barcode = ?";
          pst2 = con.prepareStatement(query3);
          
          
          for(int i=0; i<jTable1.getRowCount(); i++)
          {
             productid = (String)jTable1.getValueAt(i, 0); 
             qty = (String)jTable1.getValueAt(i, 3);
             
             pst2.setString(1, qty);
             pst2.setString(2, productid);
             pst2.executeUpdate();
             
          }
          
          JOptionPane.showMessageDialog(this, "Sales Completed!");
                    txttcost.setText("");
                    txtpay.setText("");
                    txtbal.setText("");        
                          
          
       
        HashMap p = new HashMap();
        p.put("invo" , lastid);//(key,value)
          
          try {
              
          JasperDesign jdesign = JRXmlLoader.load("C:\\Users\\noron\\Documents\\NetBeansProjects\\Stock Management System\\src\\stock\\Sales_Receipt.jrxml");
          JasperReport jreport = JasperCompileManager.compileReport(jdesign);
          JasperPrint jprint = JasperFillManager.fillReport(jreport,p,con);
          JasperViewer.viewReport(jprint);

          }

       catch(JRException ex)
       {
        Logger.getLogger(Purchase.class.getName()).log(Level.SEVERE, null , ex );
        }
        
        
        
          } 
          
          catch (SQLException ex){
            Logger.getLogger(Purchase.class.getName()).log(Level.SEVERE, null , ex );
          }
          
      }
              
    
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtpcode = new javax.swing.JTextField();
        txtpname = new javax.swing.JTextField();
        txtqty = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        txtprice = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txttcost = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtbal = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtpay = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel1.setText("SALES");

        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jLabel2.setText("PRODUCT CODE");

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jLabel3.setText("PRODUCT NAME");

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jLabel5.setText("PRICE");

        txtpcode.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        txtpcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtpcodeActionPerformed(evt);
            }
        });
        txtpcode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtpcodeKeyPressed(evt);
            }
        });

        txtpname.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        txtpname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtpnameActionPerformed(evt);
            }
        });

        txtqty.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        txtqty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtqtyActionPerformed(evt);
            }
        });

        jButton1.setText("ADD DETAILS");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "PRODUCT CODE", "PRODUCT NAME ", "PRICE", "QUANTITY", "TOTAL"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jLabel7.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jLabel7.setText("QUANTITY");

        txtprice.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        txtprice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtpriceActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jLabel8.setText("TOTAL COST");

        txttcost.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jLabel9.setText("PAYMENT");

        txtbal.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N

        jLabel10.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jLabel10.setText("BALANCE");

        txtpay.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N

        jButton2.setText("ADD");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("CLOSE");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 602, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(59, 59, 59)
                                .addComponent(jLabel8))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(67, 67, 67)
                                .addComponent(jLabel9))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(67, 67, 67)
                                .addComponent(jLabel10))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtbal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                    .addComponent(txttcost, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                    .addComponent(txtpay, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(txtpcode, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(56, 56, 56)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(84, 84, 84)
                                .addComponent(jLabel5)
                                .addGap(80, 80, 80)
                                .addComponent(jLabel7))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtpname, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(45, 45, 45)
                                .addComponent(txtprice, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(52, 52, 52)
                                .addComponent(txtqty, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(56, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(261, 261, 261)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jLabel5))))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtpcode, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtpname, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtqty, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtprice, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txttcost, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addComponent(txtpay, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jLabel10)
                        .addGap(18, 18, 18)
                        .addComponent(txtbal, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(78, 78, 78)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(34, 34, 34)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txtpcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtpcodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtpcodeActionPerformed

    private void txtpcodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtpcodeKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
            barcode();
        }
    }//GEN-LAST:event_txtpcodeKeyPressed

    private void txtpnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtpnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtpnameActionPerformed

    private void txtqtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtqtyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtqtyActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    String pcode = txtpcode.getText();
    String pname = txtpname.getText();
    String price = txtprice.getText();
    String qty = txtqty.getText();
    
    if(pcode.equals("") && pname.equals("")&& price.equals("") && qty.equals(""))
    {
        JOptionPane.showMessageDialog(this, "Please add the details of the product to be Sold");
    }    
       Sales();
        

    }//GEN-LAST:event_jButton1ActionPerformed

    private void txtpriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtpriceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtpriceActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        int pay = Integer.parseInt(txtpay.getText());
        
        int subtotal = Integer.parseInt(txttcost.getText());
        
        int bal = subtotal - pay;
        
        
        txtbal.setText(String.valueOf(bal));
        
        try {
            add();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Sales.class.getName()).log(Level.SEVERE, null, ex);
        }
   
       
        
        
        
        

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        this.setVisible(false);
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String []args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Product.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Product.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Product.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Product.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                new Sales().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField txtbal;
    private javax.swing.JTextField txtpay;
    private javax.swing.JTextField txtpcode;
    private javax.swing.JTextField txtpname;
    private javax.swing.JTextField txtprice;
    private javax.swing.JTextField txtqty;
    private javax.swing.JTextField txttcost;
    // End of variables declaration//GEN-END:variables
}

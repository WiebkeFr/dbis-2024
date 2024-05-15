package de.dis.data;

import java.sql.*;

public class House {
    private int id = -1;
    private int contractId;
    private int estateId = -1;
    private int price = -1;
    private boolean garden;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public int getEstateId() {
        return estateId;
    }

    public void setEstateId(int estateId) {
        this.estateId = estateId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean getGarden() {
        return garden;
    }

    public void setGarden(boolean garden) {
        this.garden = garden;
    }

    /**
     * Lädt ein Haus aus der Datenbank
     * @param estateId EstateID des zu ladenden Haus
     * @return Haus-Instanz
     */
    public static House load(int estateId) {
        try {
            // Hole Verbindung
            Connection con = DbConnectionManager.getInstance().getConnection();

            // Erzeuge Anfrage
            String selectSQL = "SELECT * FROM house WHERE estateId = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, estateId);

            // Führe Anfrage aus
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                House e = new House();
                e.setEstateId(estateId);
                e.setId(rs.getInt("id"));
                e.setPrice(rs.getInt("price"));
                e.setContractId(rs.getInt("contractid"));
                e.setGarden(rs.getBoolean("garden"));

                rs.close();
                pstmt.close();
                return e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Speichert das Haus in der Datenbank. Ist noch keine ID vergeben
     * worden, wird die generierte Id von der DB geholt und dem Model übergeben.
     */
    public void save() {
        // Hole Verbindung
        Connection con = DbConnectionManager.getInstance().getConnection();

        try {
            // FC<ge neues Element hinzu, wenn das Objekt noch keine ID hat.
            if (getId() == -1) {
                // Achtung, hier wird noch ein Parameter mitgegeben,
                // damit spC$ter generierte IDs zurC<ckgeliefert werden!
                String insertSQL = "INSERT INTO house(estateid, price, garden) VALUES (?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(insertSQL,
                        Statement.RETURN_GENERATED_KEYS);

                // Setze Anfrageparameter und fC<hre Anfrage aus
                pstmt.setInt(1, getEstateId());
                pstmt.setInt(2, getPrice());
                pstmt.setBoolean(3, getGarden());
                pstmt.executeUpdate();

                // Hole die Id des engefC<gten Datensatzes
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    setId(rs.getInt(1));
                }

                rs.close();
                pstmt.close();
            } else {
                // Falls schon eine ID vorhanden ist, mache ein Update...
                String updateSQL = "UPDATE house SET estateid = ?, price = ?, garden = ?, contractid = ? WHERE id = ?";
                PreparedStatement pstmt = con.prepareStatement(updateSQL);

                // Setze Anfrage Parameter
                pstmt.setInt(1, getEstateId());
                pstmt.setInt(2, getPrice());
                pstmt.setBoolean(3, getGarden());
                if(getContractId() == 0){
                    pstmt.setNull(4, 0);
                } else {
                    pstmt.setInt(4, getContractId());
                }
                pstmt.setInt(5, getId());
                pstmt.executeUpdate();

                pstmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        Connection con = DbConnectionManager.getInstance().getConnection();
        try {
            String deleteSQL = "DELETE FROM house WHERE id = ?";

            PreparedStatement pstmt = con.prepareStatement(deleteSQL,
                    Statement.RETURN_GENERATED_KEYS);

            // Setze Anfrageparameter und fC<hre Anfrage aus
            pstmt.setInt(1, getEstateId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Fehler beim Löschen!\nBitte kontrollieren Sie die zu löschende ID.");
            e.printStackTrace();
        }
    }
}

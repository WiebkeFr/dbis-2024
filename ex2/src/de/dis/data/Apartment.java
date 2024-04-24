package de.dis.data;

import java.sql.*;

public class Apartment {
    private int id = -1;
    private int floor = -1;
    private int rent = -1;
    private int rooms = -1;
    private boolean balcony;
    private boolean elevator;
    private int estateId;
    private int contractId = -1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getRent() {
        return rent;
    }

    public void setRent(int rent) {
        this.rent = rent;
    }

    public int getRooms() {
        return rooms;
    }

    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    public boolean getBalcony() {
        return balcony;
    }

    public void setBalcony(boolean balcony) {
        this.balcony = balcony;
    }

    public boolean getElevator() {
        return elevator;
    }

    public void setElevator(boolean elevator) {
        this.elevator = elevator;
    }

    public int getEstateId() {
        return estateId;
    }

    public void setEstateId(int estateId) {
        this.estateId = estateId;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    /**
     * Lädt ein Apartment aus der Datenbank
     * @param estateId EstateID des zu ladenden Apartments
     * @return Apartment-Instanz
     */
    public static Apartment load(int estateId) {
        try {
            // Hole Verbindung
            Connection con = DbConnectionManager.getInstance().getConnection();

            // Erzeuge Anfrage
            String selectSQL = "SELECT * FROM apartment WHERE estateId = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, estateId);

            // Führe Anfrage aus
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Apartment a = new Apartment();
                a.setEstateId(estateId);
                a.setId(rs.getInt("id"));
                a.setContractId(rs.getInt("contractid"));
                a.setFloor(rs.getInt("floor"));
                a.setRent(rs.getInt("rent"));
                a.setRooms(rs.getInt("rooms"));
                a.setContractId(rs.getInt("contractid"));
                a.setBalcony(rs.getBoolean("balcony"));
                a.setElevator(rs.getBoolean("elevator"));

                rs.close();
                pstmt.close();
                return a;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Speichert das Apartment in der Datenbank. Ist noch keine ID vergeben
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
                String insertSQL = "INSERT INTO apartment(floor, rent, rooms, balcony, elevator, estateid, contractid) VALUES (?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(insertSQL,
                        Statement.RETURN_GENERATED_KEYS);

                // Setze Anfrageparameter und fC<hre Anfrage aus
                pstmt.setInt(1, getFloor());
                pstmt.setInt(2, getRent());
                pstmt.setInt(3, getRooms());
                pstmt.setBoolean(4, getBalcony());
                pstmt.setBoolean(5, getElevator());
                pstmt.setInt(6, getEstateId());
                pstmt.setInt(7, getContractId());
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
                String updateSQL = "UPDATE apartment SET floor = ?, rent = ?, rooms = ?, balcony = ?, elevator = ?, estateid = ?, contractid = ? WHERE id = ?";
                PreparedStatement pstmt = con.prepareStatement(updateSQL);

                // Setze Anfrage Parameter
                pstmt.setInt(1, getFloor());
                pstmt.setInt(2, getRent());
                pstmt.setInt(3, getRooms());
                pstmt.setBoolean(4, getBalcony());
                pstmt.setBoolean(5, getElevator());
                pstmt.setInt(6, getEstateId());
                pstmt.setInt(7, getContractId());
                pstmt.setInt(8, getId());
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
            String deleteSQL = "DELETE FROM apartment WHERE id = ?";

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

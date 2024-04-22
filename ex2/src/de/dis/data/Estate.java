package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Makler-Bean
 *
 * Beispiel-Tabelle:
 * CREATE TABLE estate (
 * streetnumber varchar(255),
 * street varchar(255),
 * squarearea varchar(40) UNIQUE,
 * postal_code varchar(40),
 * ishouse
 * city
 * agentid*
 */
public class Estate {
    private int id = -1;
    private String city;
    private String postalCode;
    private String street;
    private int streetNumber;
    private int squareArea;
    private int agentId;
    private boolean isHouse;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public int getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }

    public int getSquareArea() {
        return squareArea;
    }

    public void setSquareArea(int squareArea) { this.squareArea = squareArea; }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) { this.agentId = agentId; }

    public Boolean getIsHouse() {
        return isHouse;
    }

    public void setIsHouse(Boolean isHouse) { this.isHouse = isHouse; }

    /**
     * Lädt einen Makler aus der Datenbank
     * @param id ID des zu ladenden Maklers
     * @return Makler-Instanz
     */
    public static Estate load(int id) {
        try {
            // Hole Verbindung
            Connection con = DbConnectionManager.getInstance().getConnection();

            // Erzeuge Anfrage
            String selectSQL = "SELECT * FROM estate WHERE id = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);

            // Führe Anfrage aus
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Estate e = new Estate();
                e.setId(id);
                e.setCity(rs.getString("city"));
                e.setPostalCode(rs.getString("postalcode"));
                e.setStreet(rs.getString("street"));
                e.setStreetNumber(rs.getInt("streetnumber"));
                e.setSquareArea(rs.getInt("squarearea"));
                e.setAgentId(rs.getInt("agentid"));
                e.setIsHouse(rs.getBoolean("ishouse"));

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
     * Speichert den Makler in der Datenbank. Ist noch keine ID vergeben
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
                String insertSQL = "INSERT INTO estate(city, postalCode, street, streetNumber, squareArea, agentId, isHouse) VALUES (?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(insertSQL,
                        Statement.RETURN_GENERATED_KEYS);

                // Setze Anfrageparameter und fC<hre Anfrage aus
                pstmt.setString(1, getCity());
                pstmt.setString(2, getPostalCode());
                pstmt.setString(3, getStreet());
                pstmt.setInt(4, getStreetNumber());
                pstmt.setInt(5, getSquareArea());
                pstmt.setInt(6, getAgentId());
                pstmt.setBoolean(7, getIsHouse());
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
                String updateSQL = "UPDATE estate SET city = ?, postalCode = ?, street = ?, streetNumber = ?, squareArea = ?, agentid = ?, isHouse = ? WHERE id = ?";
                PreparedStatement pstmt = con.prepareStatement(updateSQL);

                // Setze Anfrage Parameter
                pstmt.setString(1, getCity());
                pstmt.setString(2, getPostalCode());
                pstmt.setString(3, getStreet());
                pstmt.setInt(4, getStreetNumber());
                pstmt.setInt(5, getSquareArea());
                pstmt.setInt(6, getAgentId());
                pstmt.setBoolean(7, getIsHouse());
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
            String deleteSQL = "DELETE FROM estate WHERE id = ?";

            PreparedStatement pstmt = con.prepareStatement(deleteSQL,
                    Statement.RETURN_GENERATED_KEYS);

            // Setze Anfrageparameter und fC<hre Anfrage aus
            pstmt.setInt(1, getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Fehler beim Löschen!\nBitte kontrollieren Sie die zu löschende ID.\n");
            e.printStackTrace();
        }
    }
}


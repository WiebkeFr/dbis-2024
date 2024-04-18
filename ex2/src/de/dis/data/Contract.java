package de.dis.data;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Contract-Bean
 *
 * Beispiel-Tabelle:
 * CREATE TABLE contract (
 * id serial primary key,
 * date date,
 * place varchar(255),
 * personId int);
 */
public class Contract {
	private int id = -1;
	private LocalDate date = LocalDate.now();
	private String place;
	private int personId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public int getPersonId() {
		return personId;
	}

	public void setPersonId(int personId) {
		this.personId = personId;
	}
	/**
	 * Lädt einen Vertrag aus der Datenbank
	 * @param id ID des zu ladenden Vertrags
	 * @return Contract-Instanz
	 */
	public static Contract load(int id) {
		try {
			// Hole Verbindung
			Connection con = DbConnectionManager.getInstance().getConnection();

			// Erzeuge Anfrage
			String selectSQL = "SELECT * FROM contract WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setInt(1, id);

			// Führe Anfrage aus
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Contract contract = new Contract();
				contract.setId(id);
				contract.setDate(rs.getDate("date").toLocalDate());
				contract.setPlace(rs.getString("place"));
				contract.setPersonId(rs.getInt("personId"));

				rs.close();
				pstmt.close();
				return contract;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	 /**
	 * Speichert den Vertrag in der Datenbank. Ist noch keine ID vergeben
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
				String insertSQL = "INSERT INTO contract(date, place, personId) VALUES (?, ?, ?)";

				PreparedStatement pstmt = con.prepareStatement(insertSQL,
						Statement.RETURN_GENERATED_KEYS);

				// Setze Anfrageparameter und fC<hre Anfrage aus
				pstmt.setDate(1, java.sql.Date.valueOf(getDate()));
				pstmt.setString(2, getPlace());
				pstmt.setInt(3, getPersonId());
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
				String updateSQL = "UPDATE contract SET date = ?, place = ?, personId = ? WHERE id = ?";
				PreparedStatement pstmt = con.prepareStatement(updateSQL);
				pstmt.setDate(1, java.sql.Date.valueOf(getDate()));
				pstmt.setString(2, getPlace());
				pstmt.setInt(3, getPersonId());
				pstmt.setInt(4, getId());
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
			String deleteSQL = "DELETE FROM contract WHERE id = ?";

			PreparedStatement pstmt = con.prepareStatement(deleteSQL,
					Statement.RETURN_GENERATED_KEYS);

			// Setze Anfrageparameter und fC<hre Anfrage aus
			pstmt.setInt(1, getId());
			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the header of the contract table.
	 * @return String representing the header of the table
	 */
	public static String getTableHeader() {
		return "| ID | Date | Place | Person ID |";
	}

	/**
	 * Returns a string representation of this Contract instance.
	 * @return String representing this Contract
	 */
	@Override
	public String toString() {
		return String.format("| %d | %s | %s | %d |", getId(), getDate(), getPlace(), getPersonId());
	}
}

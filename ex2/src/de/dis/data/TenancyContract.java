package de.dis.data;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TenancyContract-Bean
 *
 * Beispiel-Tabelle:
 * CREATE TABLE TenancyContract (
 * contractid varchar(255),
 * duration int,
 * additionalcosts int,
 * id serial primary key);
 */
public class TenancyContract {
	private int id = -1;
	private int contractId;
	private LocalDate date = LocalDate.now();
	private int duration;
	private int additionalcosts;

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

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getAdditionalcosts() {
		return additionalcosts;
	}

	public void setAdditionalcosts(int additionalcosts) {
		this.additionalcosts = additionalcosts;
	}

	/**
	 * Lädt einen TenancyContract aus der Datenbank
	 * @param id ID des zu ladenden TenancyContracts
	 * @return TenancyContract-Instanz
	 */
	public static TenancyContract load(int id) {
		try {
			// Hole Verbindung
			Connection con = DbConnectionManager.getInstance().getConnection();

			// Erzeuge Anfrage
			String selectSQL = "SELECT * FROM TenancyContract WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setInt(1, id);

			// Führe Anfrage aus
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				TenancyContract pc = new TenancyContract();
				pc.setId(id);
				pc.setContractId(rs.getInt("contractid"));
				pc.setDate(LocalDate.parse(rs.getString("startdate")));
				pc.setDuration(rs.getInt("duration"));
				pc.setAdditionalcosts(rs.getInt("additionalcosts"));

				rs.close();
				pstmt.close();
				return pc;
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
				String insertSQL = "INSERT INTO TenancyContract(contractid, startdate, duration, additionalcosts) VALUES (?, ?, ?, ?)";

				PreparedStatement pstmt = con.prepareStatement(insertSQL,
						Statement.RETURN_GENERATED_KEYS);

				// Setze Anfrageparameter und fC<hre Anfrage aus
				pstmt.setInt(1, getContractId());
				pstmt.setDate(2, java.sql.Date.valueOf(getDate()));
				pstmt.setInt(3, getDuration());
				pstmt.setInt(4, getAdditionalcosts());
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
				String updateSQL = "UPDATE TenancyContract SET contractid = ?, startdate = ?, duration = ?, additionalcosts = ? WHERE id = ?";
				PreparedStatement pstmt = con.prepareStatement(updateSQL);
				pstmt.setInt(1, getContractId());
				pstmt.setDate(2, java.sql.Date.valueOf(getDate()));
				pstmt.setInt(3, getDuration());
				pstmt.setInt(4, getAdditionalcosts());
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
			String deleteSQL = "DELETE FROM TenancyContract WHERE id = ?";

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
	 * Fetches all tenancy contracts from the database.
	 * @return List of TenancyContract instances
	 */
	public static List<TenancyContract> fetchAll() {
		List<TenancyContract> contracts = new ArrayList<>();
		try {
			Connection con = DbConnectionManager.getInstance().getConnection();
			String query = "SELECT * FROM TenancyContract";
			PreparedStatement pstmt = con.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				TenancyContract tc = new TenancyContract();
				tc.setId(rs.getInt("id"));
				tc.setContractId(rs.getInt("contractid"));
				tc.setDate(LocalDate.parse(rs.getString("startdate")));
				tc.setDuration(rs.getInt("duration"));
				tc.setAdditionalcosts(rs.getInt("additionalcosts"));
				contracts.add(tc);
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return contracts;
	}

	/**
	 * Returns the header of the tenancy contract table.
	 * @return String representing the header of the table
	 */
	public static String getTableHeader() {
		// Assuming Contract class has a similar structure and method
		return "| ID | Contract ID | Start Date | Duration | Additional Costs -- contract -- " + Contract.getTableHeader();
	}

	/**
	 * Returns a string representation of this TenancyContract instance.
	 * @return String representing this TenancyContract
	 */
	@Override
	public String toString() {
		// Assuming Contract class has a similar structure and method
		Contract contract = Contract.load(getContractId());
		return String.format("| %d | %s | %s | %d | %d -- contract -- %s ", getId(), getContractId(), getDate(), getDuration(), getAdditionalcosts(), contract.toString());
	}
}

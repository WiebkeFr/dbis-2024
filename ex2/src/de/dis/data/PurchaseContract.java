package de.dis.data;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * PurchaseContract-Bean
 *
 * Beispiel-Tabelle:
 * CREATE TABLE purchasecontract (
 * contractid varchar(255),
 * interestrate int,
 * noinstalments int,
 * id serial primary key);
 */
public class PurchaseContract {
	private int id = -1;
	private int contractId;
	private int interestRate;
	private int noInstalments;

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

	public int getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(int interestRate) {
		this.interestRate = interestRate;
	}

	public int getNoInstalments() {
		return noInstalments;
	}

	public void setNoInstalments(int noInstalments) {
		this.noInstalments = noInstalments;
	}

	/**
	 * Lädt einen PurchaseContract aus der Datenbank
	 * @param id ID des zu ladenden PurchaseContracts
	 * @return PurchaseContract-Instanz
	 */
	public static PurchaseContract load(int id) {
		try {
			// Hole Verbindung
			Connection con = DbConnectionManager.getInstance().getConnection();

			// Erzeuge Anfrage
			String selectSQL = "SELECT * FROM purchasecontract WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setInt(1, id);

			// Führe Anfrage aus
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				PurchaseContract pc = new PurchaseContract();
				pc.setId(id);
				pc.setContractId(rs.getInt("contractid"));
				pc.setInterestRate(rs.getInt("interestrate"));
				pc.setNoInstalments(rs.getInt("noinstalments"));

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
				String insertSQL = "INSERT INTO purchasecontract(contractid, interestrate, noinstalments) VALUES (?, ?, ?)";

				PreparedStatement pstmt = con.prepareStatement(insertSQL,
						Statement.RETURN_GENERATED_KEYS);

				// Setze Anfrageparameter und fC<hre Anfrage aus
				pstmt.setInt(1, getContractId());
				pstmt.setInt(2, getInterestRate());
				pstmt.setInt(3, getNoInstalments());
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
				String updateSQL = "UPDATE purchasecontract SET contractid = ?, interestrate = ?, noinstalments = ? WHERE id = ?";
				PreparedStatement pstmt = con.prepareStatement(updateSQL);
				pstmt.setInt(1, getContractId());
				pstmt.setInt(2, getInterestRate());
				pstmt.setInt(3, getNoInstalments());
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
			String deleteSQL = "DELETE FROM purchasecontract WHERE id = ?";

			PreparedStatement pstmt = con.prepareStatement(deleteSQL,
					Statement.RETURN_GENERATED_KEYS);

			// Setze Anfrageparameter und fC<hre Anfrage aus
			pstmt.setInt(1, getId());
			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static List<PurchaseContract> fetchAll() {
		List<PurchaseContract> contracts = new ArrayList<>();
		try {
			Connection con = DbConnectionManager.getInstance().getConnection();
			String query = "SELECT * FROM PurchaseContract";
			PreparedStatement pstmt = con.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				PurchaseContract tc = new PurchaseContract();
				tc.setId(rs.getInt("id"));
				tc.setContractId(rs.getInt("contractid"));
				tc.setInterestRate(rs.getInt("interestrate"));
				tc.setNoInstalments(rs.getInt("noinstalments"));
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
	 * Returns the header of the purchase contract table.
	 * @return String representing the header of the table
	 */
	public static String getTableHeader() {
		return "| ID | Contract ID | Interest Rate | No. of Instalments -- contract -- " + Contract.getTableHeader();
	}

	/**
	 * Returns a string representation of this PurchaseContract instance.
	 * @return String representing this PurchaseContract
	 */
	@Override
	public String toString() {
		Contract contract = Contract.load(getContractId());
		return String.format("| %d | %s | %d%% | %d -- contract -- %s", getId(), getContractId(), getInterestRate(), getNoInstalments(), contract.toString());
	}


}

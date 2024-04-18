package de.dis.menu;

import de.dis.data.*;

import java.util.List;

public class ContractMenu {

    public static void showContractMenu() {
        final int NEW_PERSON = 0;
        final int CREATE_CONTRACT = 1;
        final int CONTRACT_OVERVIEW = 2;
        final int BACK = 3;


        Menu maklerMenu = new Menu("Vertrag-Verwaltung");
        maklerMenu.addEntry("Person Anlegen", NEW_PERSON);
        maklerMenu.addEntry("Vertrag Erstellen", CREATE_CONTRACT);
        maklerMenu.addEntry("Vertrag Übersicht", CONTRACT_OVERVIEW);
        maklerMenu.addEntry("Zurück zum Hauptmenü", BACK);

        while(true) {
            int response = maklerMenu.show();

            switch(response) {
                case NEW_PERSON:
                    newPerson();
                    break;
                case CREATE_CONTRACT:
                    createContractMenu();
                    break;
                case CONTRACT_OVERVIEW:
                    contractOverview();
                    break;
                case BACK:
                    return;
            }
        }
    }

    /**
     * Legt einen neuen Makler an, nachdem der Benutzer
     * die entprechenden Daten eingegeben hat.
     */
    public static void newPerson() {
        Person person = new Person();
        person.setFirstName(FormUtil.readString("First Name"));
        person.setName(FormUtil.readString("Name"));
        person.setAddress(FormUtil.readString("Address"));
        person.save();

        System.out.println("Person mit der ID "+person.getId()+" wurde erzeugt.\n");
    }

    public static void createContractMenu() {
        final int CREATE_TENANCY_CONTRACT = 0;
        final int CREATE_PURCHASE_CONTRACT = 1;
        final int BACK = 2;


        Menu a = new Menu("Vertrag Erstellen");
        a.addEntry("Mietvertrag Erstellen", CREATE_TENANCY_CONTRACT);
        a.addEntry("Kaufvertrag Erstellen", CREATE_PURCHASE_CONTRACT);
        a.addEntry("Zurück", BACK);

        while(true) {
            int response = a.show();

            switch(response) {
                case CREATE_TENANCY_CONTRACT:
                    createTenancyContract();
                    break;
                case CREATE_PURCHASE_CONTRACT:
                    createPurchaseContract();
                    break;
                case BACK:
                    return;
            }
        }
    }
    public static int newContract() {
        Contract contract = new Contract();
        contract.setPersonId(FormUtil.readInt("Person ID"));
        contract.setPlace(FormUtil.readString("Ort"));
        contract.save();
        System.out.println("Contract mit der ID "+contract.getId()+" wurde erzeugt.\n");
        return contract.getId();
    }

    public static void createTenancyContract() {
        int contractId = newContract();
        // get apartment id, ill add this when the estate classes are created
        // int apartment = FormUtil.readInt("Apartment ID");
        // Apartment apartment = Apartment.load(apartmentId);
        // apartment.setContractId(contractId);
        // apartment.save();

        TenancyContract tc = new TenancyContract();
        tc.setContractId(contractId);
        tc.setDate(FormUtil.readDate("Date"));
        tc.setDuration(FormUtil.readInt("Duration"));
        tc.setAdditionalcosts(FormUtil.readInt("Additionalcosts"));
        tc.save();
    }

    public static void createPurchaseContract() {
        int contractId = newContract();
        // get house id, ill add this when the estate classes are created
        // int houseId = FormUtil.readInt("House ID");
        // House house = House.load(houseId);
        // house.setContractId(contractId);
        // house.save();
        PurchaseContract pc = new PurchaseContract();
        pc.setContractId(contractId);
        pc.setInterestRate(FormUtil.readInt("Interest Rate"));
        pc.setNoInstalments(FormUtil.readInt("No-Instalments"));
        pc.save();
    }

    public static void contractOverview() {
        final int VIEW_TENANCY_CONTRACTS = 0;
        final int VIEW_PUCHASE_CONTRACTS = 1;
        final int BACK = 2;


        Menu a = new Menu("Vertrag Übersicht");
        a.addEntry("Mietvertrag Übersicht", VIEW_TENANCY_CONTRACTS);
        a.addEntry("Kaufvertrag Übersicht", VIEW_PUCHASE_CONTRACTS);
        a.addEntry("Zurück", BACK);

        while(true) {
            int response = a.show();

            switch(response) {
                case VIEW_TENANCY_CONTRACTS:
                    tenancyContractOverview();
                    break;
                case VIEW_PUCHASE_CONTRACTS:
                    purchaseContractOverview();
                    break;
                case BACK:
                    return;
            }
        }

    }

    public static void tenancyContractOverview() {
        List<TenancyContract> contracts = TenancyContract.fetchAll();
        System.out.println(TenancyContract.getTableHeader());
        for (TenancyContract contract : contracts) {
            System.out.println(contract.toString());
        }
    }

    public static void purchaseContractOverview() {
        List<PurchaseContract> contracts = PurchaseContract.fetchAll();
        System.out.println(PurchaseContract.getTableHeader());
        for (PurchaseContract contract : contracts) {
            System.out.println(contract.toString());
        }
    }

}

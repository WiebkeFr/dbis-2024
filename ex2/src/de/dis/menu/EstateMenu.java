package de.dis.menu;

import de.dis.data.Apartment;
import de.dis.data.Estate;
import de.dis.data.House;
import de.dis.data.Makler;

public class EstateMenu {
    /**
     * Zeigt die Landgut-Verwaltung
     */
    public static void showEstateMenu() {
        System.out.println("Bitte melden Sie sich an:");
        int id = FormUtil.readInt("ID");
        String login = FormUtil.readString("Login");
        String pw = FormUtil.readString("Password");

        Makler currentMakler = Makler.login(id, login, pw);
        if (currentMakler.getId() == -1) {
            System.out.println("Der Login ist fehlgeschlagen.\nSie werden zum Hauptmenu zurückgeleitet.\n");
            return;
        }

        //Menüoptionen
        final int NEW_ESTATE = 0;
        final int EDIT_ESTATE = 1;
        final int DELETE_ESTATE = 2;
        final int BACK = 3;

        //Landgutverwaltungsmenü
        Menu maklerMenu = new Menu("Landgut-Verwaltung");
        maklerMenu.addEntry("Neues Landgut", NEW_ESTATE);
        maklerMenu.addEntry("Landgut bearbeiten", EDIT_ESTATE);
        maklerMenu.addEntry("Landgut Löschen", DELETE_ESTATE);
        maklerMenu.addEntry("Zurück zum Hauptmenü", BACK);

        //Verarbeite Eingabe
        while(true) {
            int response = maklerMenu.show();

            switch(response) {
                case NEW_ESTATE:
                    newEstate();
                    break;
                case EDIT_ESTATE:
                    editEstate();
                    break;
                case DELETE_ESTATE:
                    deleteEstate();
                    break;
                case BACK:
                    return;
            }
        }
    }

    /**
     * Legt einen neues Landgut an, nachdem der Benutzer
     * die entprechenden Daten eingegeben hat.
     */
    public static void newEstate() {
        Estate e = new Estate();

        e.setAgentId(FormUtil.readInt("Korrespondierender Makler (ID)"));
        e.setCity(FormUtil.readString("Stadt"));
        e.setPostalCode(FormUtil.readString("Postleitzahl"));
        e.setStreet(FormUtil.readString("Straße"));
        e.setStreetNumber(FormUtil.readInt("Straßennummer"));
        e.setSquareArea(FormUtil.readInt("Grundfläche in m^2"));
        boolean isHouse = FormUtil.readString("Handelt es sich bei dem Landgut um Haus? (ja/nein)").equals("ja");

        e.setIsHouse(isHouse);
        e.save();

        if (isHouse) {
            House h = new House();
            h.setEstateId(e.getId());
            h.setPrice(FormUtil.readInt("Preis"));
            h.setGarden(FormUtil.readString("Mit Garten (ja/nein)").equals("ja"));
            h.save();

        } else {
            Apartment a = new Apartment();
            a.setEstateId(e.getId());
            a.setRent(FormUtil.readInt("Miete"));
            a.setFloor(FormUtil.readInt("Etage"));
            a.setRooms(FormUtil.readInt("Räume"));
            a.setBalcony(FormUtil.readString("Mit Balkon (ja/nein)").equals("ja"));
            a.setElevator(FormUtil.readString("Mit Aufzug (ja/nein)").equals("ja"));
            a.save();
        }

        String type = isHouse ? "Haus" : "Apartment";
        System.out.println("Landgut (" + type + ") mit der ID "+e.getId()+" wurde erzeugt.\n");
    }

    /**
     * Bearbeitet ein Landgut an, nachdem der Benutzer
     * die entprechenden ID eingegeben hat.
     */
    public static void editEstate() {
        int id = FormUtil.readInt("Geben Sie die ID des zu bearbeitende Landgut ein");
        Estate e = Estate.load(id);
        boolean oldIsHouse = e.getIsHouse();

        e.setAgentId(FormUtil.editValue("Korrespondierender Makler (ID)", e.getAgentId()));
        e.setCity(FormUtil.editValue("Stadt", e.getCity()));
        e.setPostalCode(FormUtil.editValue("Postleitzahl", e.getPostalCode()));
        e.setStreet(FormUtil.editValue("Straße", e.getStreet()));
        e.setStreetNumber(FormUtil.editValue("Straßennummer", e.getStreetNumber()));
        e.setSquareArea(FormUtil.editValue("Grundfläche in m^2", e.getSquareArea()));
        e.setIsHouse(FormUtil.editValue("Handelt es sich bei dem Landgut um Haus?", e.getIsHouse()));

        // type of estate was edited:
        // case 1: apt -> house
        if (oldIsHouse != e.getIsHouse() && e.getIsHouse()) {
            Apartment a = new Apartment();
            a.setEstateId(e.getId());
            a.delete();

        // case 2: house -> apt
        } else if (oldIsHouse != e.getIsHouse() && !e.getIsHouse()) {
            House h = new House();
            h.setEstateId(e.getId());
            h.delete();
        }

        // create or update specified type of estate
        if (e.getIsHouse()) {
            House h = oldIsHouse == e.getIsHouse() ? House.load(e.getId()) : new House();
            h.setEstateId(e.getId());
            h.setPrice(FormUtil.editValue("Preis", h.getPrice()));
            h.setContractId(FormUtil.editValue("Id des korrespondierenden Vertrags", h.getContractId()));
            h.setGarden(FormUtil.editValue("Mit Garten ", h.getGarden()));
            h.save();
        } else {
            Apartment a = oldIsHouse == e.getIsHouse() ? Apartment.load(e.getId()) : new Apartment();
            a.setEstateId(e.getId());
            a.setRent(FormUtil.editValue("Miete", a.getRent()));
            a.setFloor(FormUtil.editValue("Etage", a.getFloor()));
            a.setRooms(FormUtil.editValue("Räume", a.getRooms()));
            a.setBalcony(FormUtil.editValue("Mit Balkon", a.getBalcony()));
            a.setElevator(FormUtil.editValue("Mit Aufzug", a.getElevator()));
            a.save();
        }

        e.save();

        System.out.println("Landgut mit der ID "+e.getId()+" wurde bearbeitet.\n");
    }

    /**
     * Löscht ein Landgut, nachdem der Benutzer
     * die entprechenden ID eingegeben hat.
     */
    public static void deleteEstate() {
        int id = FormUtil.readInt("Geben Sie die ID des zu löschende Landgut ein (korrespondierende Verträger werden gelöscht)");
        Estate e = new Estate();
        e.setId(id);
        e.delete();

        System.out.println("Landgut mit der ID "+e.getId()+" wurde gelöscht.");
    }
}

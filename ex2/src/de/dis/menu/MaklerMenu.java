package de.dis.menu;

import de.dis.data.Makler;

public class MaklerMenu {
    /**
     * Zeigt die Maklerverwaltung
     */
    public static void showMaklerMenu() {

        String pw = FormUtil.readString("Geben Sie Ihr Passwort ein");
        if (!pw.equals("1234")) {
            System.out.println("Sie haben keine Berechtigung zum Makler-Menu!\n");
            return;
        } else {
            System.out.println("");
        }

        //Menüoptionen
        final int NEW_MAKLER = 0;
        final int EDIT_MAKLER = 1;
        final int DELETE_MAKLER = 2;
        final int BACK = 3;

        //Maklerverwaltungsmenü
        Menu maklerMenu = new Menu("Makler-Verwaltung");
        maklerMenu.addEntry("Neuer Makler", NEW_MAKLER);
        maklerMenu.addEntry("Makler Bearbeitung", EDIT_MAKLER);
        maklerMenu.addEntry("Makler Löschen", DELETE_MAKLER);
        maklerMenu.addEntry("Zurück zum Hauptmenü", BACK);

        //Verarbeite Eingabe
        while(true) {
            int response = maklerMenu.show();

            switch(response) {
                case NEW_MAKLER:
                    newMakler();
                    break;
                case EDIT_MAKLER:
                    editMakler();
                    break;
                case DELETE_MAKLER:
                    deleteMakler();
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
    public static void newMakler() {
        Makler m = new Makler();

        m.setName(FormUtil.readString("Name"));
        m.setAddress(FormUtil.readString("Adresse"));
        m.setLogin(FormUtil.readString("Login"));
        m.setPassword(FormUtil.readString("Passwort"));

        Makler uniqueChecker = Makler.login(m.getLogin(), m.getPassword());
        if (uniqueChecker.getId() == -1) {
            m.save();
            System.out.println("Makler mit der ID "+m.getId()+" wurde erzeugt.\n");
        } else {
            System.out.println("Der Login und/oder das Passort ist bereits vergeben.\nBitte versuchen Sie es mit anderen nochmal!\n");
        }
    }

    /**
     * Bearbeitet einen Makler an, nachdem der Benutzer
     * die entprechenden ID eingegeben hat.
     */
    public static void editMakler() {
        int id = FormUtil.readInt("Geben Sie die ID des zu bearbeitenden Maklers ein");
        Makler m = Makler.load(id);

        m.setName(FormUtil.editValue("Name", m.getName()));
        m.setAddress(FormUtil.editValue("Adresse", m.getAddress()));
        m.setLogin(FormUtil.editValue("Login", m.getLogin()));
        m.setPassword(FormUtil.editValue("Passwort", m.getPassword()));
        m.save();

        System.out.println("Makler mit der ID "+m.getId()+" wurde bearbeitet.\n");
    }

    /**
     * Löscht einen Makler an, nachdem der Benutzer
     * die entprechenden ID eingegeben hat.
     */
    public static void deleteMakler() {
        int id = FormUtil.readInt("Geben Sie die ID des zu löschenden Maklers ein");
        Makler m = new Makler();
        m.setId(id);
        m.delete();

        System.out.println("Makler mit der ID "+m.getId()+" wurde gelöscht.\n");
    }
}



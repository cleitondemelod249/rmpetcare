package com.rmpetcare;

public class atendente {

    private static OBJCustomer objCustomer;

    public static boolean addatendente(String nome,String CPF, String login, String senha){

        boolean atend = false;
        String query = "insert int recepcao values('";
        query += nome + "', '";
        query += cpf + "', '";
        query += login + "', '";
        query += senha + "' , '";
        query +=")";

        system.out.println("@Show Query");
        system.out.println(query);

        try {
			//DBControl.executeQuery(query);
			atend = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return atend;
    }

    public static boolean editaratendente(Int id, String nome, String login, String senha){

        return false;
    }

    public static boolean deleteatendente(Int id, String nome, String login, String senha){

        return false;
    }
}
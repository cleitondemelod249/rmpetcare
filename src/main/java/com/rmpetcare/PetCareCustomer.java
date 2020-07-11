package com.rmpetcare;

public class PetCareCustomer {
	
	private static OBJCustomer objCustomer;
	
	public static OBJCustomer getCustomer() {
		
		
		return objCustomer;
	}

	public static Boolean createCustomer(String name, String CPF, String name_pet, int idade_pet, String tipo_pet, String endereco, String cidade, String bairro, String login, String passwd) {
		
		Boolean isCreate = false;
		String query = "INSERT INTO paciente VALUES('";
		query += name + "', '";
		query += CPF + "', '";
		query += name_pet + "', '";
		query += idade_pet + "', '";
		query += tipo_pet + "', '";
		query += endereco + "', '";
		query += cidade + "', '";
		query += bairro + "', '";
		query += login + "', '";
		query += passwd + "'";
		query += ")";
		
		System.out.println("@Show Query");
		System.out.println(query);
		
		try {
			//DBControl.executeQuery(query);
			isCreate = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return isCreate;
	}
	
	public static Boolean updateCustomer(int ID, String name, String CPF, String name_pet, int idade_pet, String tipo_pet, String endereco, String cidade, String bairro, String login, String passwd) {
		
		return false;
	}
	
	public static Boolean deleteCustomer(int ID, String login, String passwd) {
		
		return false;
	}
}

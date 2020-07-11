package com.avaya.atento;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/")
public class FileUploadService {

	static String cOK = "OK";
	static String cBADREQUEST = "Bad Request";
	static String cNOTFOUND = "Not found";
	private String code;
	public File curPath;

	final static Logger logger = Logger.getLogger(FileUploadService.class);
	private String UPLOAD_FOLDER = System.getProperty("catalina.home") + "/temp/Files/ativos";
	public String getProp = System.getProperty("catalina.home");
	
	public FileUploadService() {

		if (System.getProperty("catalina.home") != null) {
			curPath = new File(System.getProperty("catalina.home") + "/temp/Files/ativos");
		} else if(System.getProperties().get("basedir") != null) {
			curPath = new File(System.getProperties().get("basedir") + "/temp/Files/ativos");
		} else {
			curPath = new File("temp/Files/ativos");
		}
		this.UPLOAD_FOLDER = curPath.getAbsolutePath();
		
		MyRunnable runn = new MyRunnable();
		Thread thread = new Thread(runn);

		thread.start();
		synchronized (thread) {
			try {
				thread.wait();
			} catch (Exception e) {
				logger.info(e.toString());
			}
		}
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("insc") String insc) {

		org.apache.log4j.PropertyConfigurator.configure(System.getProperty("catalina.home") + "/temp/log4j.properties");
		// check if all form parameters are provided
		if (uploadedInputStream == null || fileDetail == null)
			return Response.status(400).entity("Invalid form data").build();
		// create our destination folder, if it not exists
		try {
			createFolderIfNotExists(UPLOAD_FOLDER);
		} catch (SecurityException se) {
			return Response.status(500).entity("Can not create destination folder on server").build();
		}

		String extFile = fileDetail.getFileName();
		extFile = extFile.substring(extFile.length() - 4);
		if (extFile.contains(".")) {
			extFile = extFile.substring(extFile.length() - 3);
		}

		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());

		String iFileName = insc + "_" + timeStamp + "." + extFile;

		String uploadedFileLocation = UPLOAD_FOLDER + "/" + iFileName;
		this.code = "{ \"url\": \"" + iFileName + "\" }";

		try {
			saveToFile(uploadedInputStream, uploadedFileLocation);
		} catch (IOException e) {
			logger.info("Arquivo não foi salvo! Erro: " + e.toString());
			return Response.status(500).entity("Cannot save file").build();
		}

		return Response.status(200).entity(code).build();
	}

	private void saveToFile(InputStream inStream, String target) throws IOException {
		OutputStream out = null;
		int read = 0;
		byte[] bytes = new byte[1024];
		out = new FileOutputStream(new File(target));
		while ((read = inStream.read(bytes)) != -1) {
			out.write(bytes, 0, read);
		}
		out.flush();
		out.close();
	}

	private void createFolderIfNotExists(String dirName) throws SecurityException {
		File theDir = new File(dirName);
		if (!theDir.exists()) {
			theDir.mkdir();
		}
	}

	@GET
	@Path("/formfilter/")
	public Response formFilter(@QueryParam("instalacao") String insc, @QueryParam("dateStart") String dtStart,
			@QueryParam("dateEnd") String dtEnd) {
		
		setCurrentPath();
		Boolean checkInsc = false, checkDtStart = false, inativos = false;
		Date dtsMore30 = null;
		Date currentDt = null;
		try {
			String[] fileNameAsInstalacao;
			String[] fileGetName;
			File dir = new File(UPLOAD_FOLDER);

			if (dir.exists()) {
				File[] files = dir.listFiles();
				this.code = "[";
				if (files.length > 0) {
					for (int i = 0; i < files.length; i++) {
						java.nio.file.Path location = Paths.get(files[i].getAbsolutePath());
						FileTime lastModifiedTime = Files.getLastModifiedTime(location, LinkOption.NOFOLLOW_LINKS);
						String lastModifiedTimeAsString = format(lastModifiedTime.toMillis());
						Date dateLastModifield = new SimpleDateFormat("yyyy-MM-dd").parse(lastModifiedTimeAsString);
						SimpleDateFormat format4json = new SimpleDateFormat("dd/MM/yyyy");
						Date currentDate = new Date();
						Date dateStart = new SimpleDateFormat("yyyy-MM-dd").parse(dtStart);
						Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(dtEnd);
					
						if (!inativos) {
							currentDt = new Date();
							
							// Através do Calendar, trabalhamos a data informada e adicionamos 1 dia nela
							Calendar c = Calendar.getInstance();
							c.setTime(dateStart);
							c.add(Calendar.DATE, +30);
							dtsMore30 = c.getTime();
						}

						if (dtsMore30.before(currentDt)) {
							dir = new File( System.getProperties().get("basedir") + "/temp/Files/inativos");
							files = dir.listFiles();
							if (files.length <= 0) {
								break;
							}
							dtsMore30 = new Date();
							inativos = true;
						}

						if (insc.isEmpty() || checkInsc) {
							fileNameAsInstalacao = files[i].getName().split("_");
							insc = fileNameAsInstalacao[0];
							checkInsc = true;
						}

						if (dtStart.isEmpty() || checkDtStart) {
							dtStart = currentDate.toString();
							checkDtStart = true;
						}

						fileGetName = files[i].getName().split("_");
						if ((dateLastModifield.after(dateStart) || dateLastModifield.equals(dateStart))
								&& (dateLastModifield.before(dateEnd) || dateLastModifield.equals(dateEnd))
								&& fileGetName[0].equals(insc)) {
							if (i == (files.length - 1)) {
								this.code += "{ \"name\": \"" + files[i].getName() + "\", \"date\": \""
										+ format4json.format(dateLastModifield) + "\" }";
							} else {
								this.code += "{ \"name\": \"" + files[i].getName() + "\", \"date\": \""
										+ format4json.format(dateLastModifield) + "\" },";
							}
						}
					}

				} else {
					this.code = "[{ \"ex\": \"Dir Vazio\" }";
				}
			} else {
				this.code = "[{ \"ex\": \"Dir nao existe\" }";
			}
		} catch (Exception e) {
			logger.info(e.toString());
			this.code = "[{ \"ex\": \"" + e.getMessage() + "\" }";
		}

		checkInsc = false;
		checkDtStart = false;

		if (this.code.substring(this.code.length() - 1).equals(",")) {
			this.code = this.code.substring(0, this.code.length() - 1);
		}

		return Response.status(200).entity(this.code + "]").build();
	}

	public static String format(long time) {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(new Date(time));
	}

	@GET
	public Response sGetFile(@QueryParam("name") String name) throws IOException, ParseException {

		File file = new File(UPLOAD_FOLDER + "/" + name);
		return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").build();
	}

	@GET
	@Path("/findfile/")
	public Response sFindFile(@QueryParam("name") String name) throws IOException, ParseException {

		setCurrentPath();
		try {
			File file = new File(UPLOAD_FOLDER + name);
			if (file.exists()) {
				this.code = "{ \"url\": \"True\" }";
			} else {
				this.code = "{ \"url\": \"False\" }";
			}
		} catch (Exception e) {
			logger.info(e.toString());
			return Response.status(500).entity("Error: " + e.getMessage()).build();
		}

		return Response.status(200).entity(code).build();
	}

	@POST
	@Path("/showfiles")
	public Response showFilesOnDirectory() {
		File folder = new File(UPLOAD_FOLDER);
		String[] files = folder.list();
		int acStart = 0;
		for (String file : files) {
			acStart += 1;
		}
		this.code = "[ ";
		while (acStart >= 1) {
			this.code += "{ \"name\": \"" + files[acStart - 1] + "\" }";
			if (acStart != 1) {
				this.code += ", ";
			}
			acStart -= 1;
		}
		this.code += " ]";
		return Response.status(200).entity(code).build();
	}

	public void setCurrentPath() {
		if (this.getProp == null) {
			File teste = new File("");
			this.getProp = curPath.getAbsolutePath();
			org.apache.log4j.PropertyConfigurator.configure(teste.getAbsolutePath() + "/log4j.properties");
		} else {
			org.apache.log4j.PropertyConfigurator
					.configure(System.getProperty("catalina.home") + "/temp/log4j.properties");
		}
	}
}

class MyRunnable implements Runnable {

	private String UPLOAD_FOLDER = System.getProperty("catalina.home") + "/temp/Files/ativos";
	Logger logger = Logger.getLogger(MyRunnable.class);
	File curPath;
	
	@Override
	public void run() {
		synchronized (this) {

			String getProp = (System.getProperty("catalina.home"));

			if (System.getProperty("catalina.home") != null) {
				curPath = new File(System.getProperty("catalina.home") + "/temp/Files/ativos");
			} else if(System.getProperties().get("basedir") != null) {
				curPath = new File(System.getProperties().get("basedir") + "/temp/Files/ativos");
			} else {
				curPath = new File("temp/Files/ativos");
			}
			this.UPLOAD_FOLDER = curPath.getAbsolutePath();

			if (getProp == null) {
				File teste = new File("");
				getProp = curPath.getAbsolutePath();
				org.apache.log4j.PropertyConfigurator.configure(teste.getAbsolutePath() + "/log4j.properties");
			} else {
				org.apache.log4j.PropertyConfigurator
						.configure(System.getProperty("catalina.home") + "/temp/log4j.properties");
			}

			Boolean runThread = true;
			int acount = 0;
			File dir = new File(this.UPLOAD_FOLDER);
			
			File[] files = dir.listFiles();

			while (runThread) {

				if (files.length > 0) {
					
					try {
						java.nio.file.Path location = Paths.get(files[acount].getAbsolutePath());
						FileTime lastModifiedTime;
						lastModifiedTime = Files.getLastModifiedTime(location, LinkOption.NOFOLLOW_LINKS);
						String lastModifiedTimeAsString = format(lastModifiedTime.toMillis());
						Date dateLastModifield = new SimpleDateFormat("yyyy-MM-dd").parse(lastModifiedTimeAsString);
						Date currentDate = new Date();
						currentDate.setDate(currentDate.getDate() - 30);
						
						if (dateLastModifield.before(currentDate)) {
							String strWithName = files[acount].getAbsolutePath();
							File pathWithName = new File(strWithName);
							
							File curPathInat = null;
							
							if(System.getProperty("catalina.home") != null) {
								curPathInat = new File(System.getProperty("catalina.home"));
							} else {
								curPathInat = new File(System.getProperties().get("basedir")+"");
							}
							
							if (pathWithName
									.renameTo(new File(curPathInat.getAbsoluteFile() + "/temp/Files/inativos/" + files[acount].getName()))) {
								logger.info("[ " + files[acount].getName() + " ] File is moved successful!");
							} else {
								logger.info("[ " + files[acount].getName() + " ] File failed to move!");
							}
						}
						acount++;
						if (acount >= files.length) {
							runThread = false;
						}
					} catch (IOException e) {
						logger.info(e.toString());
					} catch (ParseException e) {
						logger.info(e.toString());
					}
				} else {
					runThread = false;
				}
			}
		}
	}

	public static String format(long time) {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(new Date(time));
	}
}

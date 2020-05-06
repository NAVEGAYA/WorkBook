package com.house.springboot.workbook.backend.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.house.springboot.workbook.backend.methods.AccessDataBase;


@CrossOrigin(origins = { "*" })
@RestController
@RequestMapping("/api")
public class WBRestController {
	
	private final Logger logger = Logger.getLogger(WBRestController.class.getName());
	
	@Value("${spring.uploads}")
	private String uploads;
	
	@Value("${spring.templates}")
	private String templates;
	
	@Value("${spring.pdfs}")
	private String pdfs;

	@GetMapping(value="/menu", produces="application/json;charset=UTF-8")
	public ResponseEntity<String> menu(@RequestParam(value="user_name", required=true) String user_name) {

		AccessDataBase ADB = new AccessDataBase();

		String response = ADB.getMenu(user_name);

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@GetMapping(value="/information", produces="application/json;charset=UTF-8")
	public ResponseEntity<?> information(@RequestParam(value="Object", required=true) String object, @RequestParam(value="Find", required=true) String find,  @RequestParam(value="Params", required=false) String params) {

		AccessDataBase ADB = new AccessDataBase();

		String WH=find.replace("'","''").replace("\\|", "'").replace("¯", "'").replace("¦","%").replace("œ","+"),
			   PR=params != null ? params.replace("'","''").replace("\\|", "'").replace("¯", "'").replace("¦","%").replace("œ","+"):"";

		Map<String, Object> response = ADB.getInformation(object, WH, PR);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@PostMapping(value="/save/{option}", produces="application/json;charset=UTF-8", consumes="application/json;charset=UTF-8")
	public ResponseEntity<?> saveInformation(@PathVariable String option, @RequestParam(value="object", required=true) String object, @RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="folio", required=true) String folio, @RequestParam(value="status", required=true) String status, @RequestBody String json) {
		Map<String, Object> response = new HashMap<String, Object>();

		AccessDataBase ADB = new AccessDataBase();
		
		String fol = folio;

		if(status.equals("1")) {
			fol = ADB.prepareData(option, object, user_name, folio, json, uploads);
		}

		response.put("FOLIO", fol);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@GetMapping(value="/recover", produces="application/json;charset=UTF-8")
	public ResponseEntity<?> retrieveInformation(@RequestParam(value="object", required=true) String object, @RequestParam(value="find", required=true) String find) {
		String WH=find.replace("'","''").replace("\\|", "'").replace("¯", "'").replace("¦","%").replace("œ","+");

		AccessDataBase ADB = new AccessDataBase();

		Map<String, Object> response = ADB.retriveData(object, WH);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@PostMapping(value="/upload/file")
	public ResponseEntity<?> upload(@RequestParam(value="file", required=true) MultipartFile file, @RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="object", required=true) String object, @RequestParam(value="folio", required=true) String folio){
		Map<String, Object> response = new HashMap<String, Object>();

		AccessDataBase ADB = new AccessDataBase();

		if(!file.isEmpty()) {
			String nameFile = file.getOriginalFilename();
			String pathUpload = ADB.createDirUpload(user_name, object, folio, uploads);

			Path rutaArchivo = Paths.get(pathUpload).resolve(nameFile).toAbsolutePath();
			logger.log(Level.INFO, "Ruta del archivo {0}", new Object[]{rutaArchivo.toString()});

			try {
				Files.copy(file.getInputStream(), rutaArchivo, REPLACE_EXISTING);
			} catch (IOException e) {
				response.put("message", "Error al subir el archivo");
				response.put("file", "");
				response.put("error", e.getMessage().concat(": ").concat(e.getCause().getMessage()));
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			pathUpload += nameFile;

			File fileUpload = new File(pathUpload);
			String extension = nameFile.substring(nameFile.lastIndexOf( "." ));

			if(fileUpload.exists()) {
				if(extension.toLowerCase().equals(".pdf")){
					try (PDDocument document = PDDocument.load(fileUpload)) {
						if(document.isEncrypted()){
							document.close();
							fileUpload.delete();

							response.put("message", "El archivo debe estar desbloqueado para cargarlo en el servidor");
							response.put("file", "prohibitus");
							return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
						}
					} catch (InvalidPasswordException e) {
						e.printStackTrace();

						response.put("message", "El archivo debe estar desbloqueado para cargarlo en el servidor");
						response.put("file", "prohibitus");
						return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					} catch (IOException e) {
						e.printStackTrace();

						response.put("message", "El archivo debe estar desbloqueado para cargarlo en el servidor");
						response.put("file", "prohibitus");
						return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
			}

			response.put("message", "El archivo se ha subido correctamente");
			response.put("file", nameFile);
		}

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@GetMapping(value="reader/xml", produces="application/json;charset=UTF-8")
	public ResponseEntity<String> xml(@RequestParam(value="file", required=true) String file, @RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="object", required=true) String object, @RequestParam(value="folio", required=true) String folio){
		AccessDataBase ADB = new AccessDataBase();

		String pathUpload = ADB.createDirUpload(user_name, object, folio, uploads);

		Path pathFile = Paths.get(pathUpload).resolve(file).toAbsolutePath();
		logger.log(Level.INFO, "Ruta del archivo {0}", new Object[]{pathFile.toString()});
		
		File pFile = pathFile.toFile();
		
		String data = "";
		
		if(pFile.exists()) {
			data = ADB.readerXML(pFile);
		} else {
			data = "No existe el archvio";
		}
		
		return new ResponseEntity<String>(data, HttpStatus.OK);
	}
	
	@GetMapping("show/file")
	public ResponseEntity<InputStreamResource> show(@RequestParam(value="file", required=true) String file, @RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="object", required=true) String object, @RequestParam(value="folio", required=true) String folio, @RequestParam(value="type", required=true) String type){
		AccessDataBase ADB = new AccessDataBase();

		String pathUpload = ADB.createDirUpload(user_name, object, folio, uploads);

		Path pathFile = Paths.get(pathUpload).resolve(file).toAbsolutePath();
		logger.log(Level.INFO, "Ruta del archivo {0}", new Object[]{pathFile.toString()});

		Resource resource = null;
		long r = 0;
	    InputStream is=null;

		try {
			resource = new UrlResource(pathFile.toUri());
	        is = resource.getInputStream();
	        r = resource.contentLength();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(!resource.exists() && !resource.isReadable()) {
			throw new RuntimeException("Error no se pudo cargar el archivo: " + file);
		}


		return ResponseEntity.ok().contentLength(r)
                .contentType(MediaType.parseMediaType(type))
                .body(new InputStreamResource(is));
	}

	@GetMapping("create/file/{option}")
	public ResponseEntity<InputStreamResource> create(@PathVariable String option, @RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="object", required=true) String object, @RequestParam(value="folio", required=true) String folio){
		AccessDataBase ADB = new AccessDataBase();

		String file = ADB.createPDF(option, user_name, object, folio, uploads, templates, pdfs);

		Path pathFile = Paths.get("").resolve(file).toAbsolutePath();
		logger.log(Level.INFO, "Ruta del archivo {0}", new Object[]{pathFile.toString()});

		Resource resource = null;
		long r = 0;
	    InputStream is=null;

		try {
			resource = new UrlResource(pathFile.toUri());
	        is = resource.getInputStream();
	        r = resource.contentLength();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(!resource.exists() && !resource.isReadable()) {
			throw new RuntimeException("Error no se pudo cargar el archivo: " + file);
		}


		return ResponseEntity.ok().contentLength(r)
                .contentType(MediaType.parseMediaType("application/pdf"))
                .body(new InputStreamResource(is));
	}

	@GetMapping("download/file/{option}")
	public ResponseEntity<InputStreamResource> download(@PathVariable String option, @RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="object", required=true) String object, @RequestParam(value="folio", required=true) String folio){
		AccessDataBase ADB = new AccessDataBase();

		String file = ADB.downloadPDF(option, user_name, object, folio, pdfs);

		Path pathFile = Paths.get("").resolve(file).toAbsolutePath();
		logger.log(Level.INFO, "Ruta del archivo {0}", new Object[]{pathFile.toString()});

		Resource resource = null;
		long r = 0;
	    InputStream is=null;

		try {
			resource = new UrlResource(pathFile.toUri());
	        is = resource.getInputStream();
	        r = resource.contentLength();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(!resource.exists() && !resource.isReadable()) {
			throw new RuntimeException("Error no se pudo cargar el archivo: " + file);
		}


		return ResponseEntity.ok().contentLength(r)
                .contentType(MediaType.parseMediaType("application/pdf"))
                .body(new InputStreamResource(is));
	}
	
	@PostMapping(value="/upload/form")
	public ResponseEntity<?> uploadForm(@RequestParam(value="file", required=true) MultipartFile file, @RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="object", required=true) String object, @RequestParam(value="folio", required=true) String folio){
		Map<String, Object> response = new HashMap<String, Object>();

		AccessDataBase ADB = new AccessDataBase();

		if(!file.isEmpty()) {
			String temp_nameFile = "temp_"+folio+".pdf",
				   nameFile = folio+".pdf";
			
			String pathUpload = ADB.uploadForm(user_name, object, folio, pdfs);
			

			Path rutaArchivo = Paths.get(pathUpload).resolve(temp_nameFile).toAbsolutePath();
			logger.log(Level.INFO, "Ruta del archivo {0}", new Object[]{rutaArchivo.toString()});

			try {
				Files.copy(file.getInputStream(), rutaArchivo, REPLACE_EXISTING);
			} catch (IOException e) {
				response.put("message", "Error al subir el archivo");
				response.put("file", "");
				response.put("error", e.getMessage().concat(": ").concat(e.getCause().getMessage()));
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			String pathUploadNew = pathUpload + nameFile;
			pathUpload += temp_nameFile;

			File fileUpload = new File(pathUpload),
				 fileUploadNew = new File(pathUploadNew);

			if(fileUpload.exists()) {
				try (PDDocument document = PDDocument.load(fileUpload)) {
					if(document.isEncrypted()){
						document.close();
						fileUpload.delete();

						response.put("message", "El archivo debe estar desbloqueado para cargarlo en el servidor");
						response.put("file", "prohibitus");
						return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					} else {
						Files.deleteIfExists(fileUploadNew.toPath());
						
						fileUpload.renameTo(fileUploadNew);
						
						ADB.changeStatus(user_name, object, folio);
					}
					
					
				} catch (InvalidPasswordException e) {
					e.printStackTrace();

					response.put("message", "El archivo debe estar desbloqueado para cargarlo en el servidor");
					response.put("file", "prohibitus");
					return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				} catch (IOException e) {
					e.printStackTrace();

					response.put("message", "El archivo debe estar desbloqueado para cargarlo en el servidor");
					response.put("file", "prohibitus");
					return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}

			response.put("message", "El archivo se ha subido correctamente");
			response.put("file", nameFile);
		}

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}
	
	@PostMapping(value="/upload/pdf")
	public ResponseEntity<?> uploadPDF(@RequestParam(value="file", required=true) MultipartFile file, @RequestParam(value="pdf", required=true) String pdf, @RequestParam(value="type", required=true) String type){
		Map<String, Object> response = new HashMap<String, Object>();

		AccessDataBase ADB = new AccessDataBase();

		if(!file.isEmpty()) {
			String nameFile = pdf+".pdf";
			String pathUpload = ADB.uploadPDF(pdf, type, templates);

			Path rutaArchivo = Paths.get(pathUpload).resolve(nameFile).toAbsolutePath();
			logger.log(Level.INFO, "Ruta del archivo {0}", new Object[]{rutaArchivo.toString()});

			try {
				Files.copy(file.getInputStream(), rutaArchivo, REPLACE_EXISTING);
			} catch (IOException e) {
				response.put("message", "Error al subir el archivo");
				response.put("file", "");
				response.put("error", e.getMessage().concat(": ").concat(e.getCause().getMessage()));
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			pathUpload += nameFile;

			File fileUpload = new File(pathUpload);
			String extension = nameFile.substring(nameFile.lastIndexOf( "." ));

			if(fileUpload.exists()) {
				if(extension.toLowerCase().equals(".pdf")){
					try (PDDocument document = PDDocument.load(fileUpload)) {
						if(document.isEncrypted()){
							document.close();
							fileUpload.delete();

							response.put("message", "El archivo debe estar desbloqueado para cargarlo en el servidor");
							response.put("file", "prohibitus");
							return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
						}
					} catch (InvalidPasswordException e) {
						e.printStackTrace();

						response.put("message", "El archivo debe estar desbloqueado para cargarlo en el servidor");
						response.put("file", "prohibitus");
						return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					} catch (IOException e) {
						e.printStackTrace();

						response.put("message", "El archivo debe estar desbloqueado para cargarlo en el servidor");
						response.put("file", "prohibitus");
						return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
			}

			response.put("message", "El archivo se ha subido correctamente");
			response.put("file", nameFile);
		}

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}
	
	@GetMapping("download/pdf")
	public ResponseEntity<InputStreamResource> downloadPDF(@RequestParam(value="type", required=true) String type, @RequestParam(value="pdf", required=true) String pdf){
		AccessDataBase ADB = new AccessDataBase();

		String nameFile = pdf+".pdf";
		String path = ADB.uploadPDF(pdf, type, templates);

		Path pathFile = Paths.get(path).resolve(nameFile).toAbsolutePath();
		logger.log(Level.INFO, "Ruta del archivo {0}", new Object[]{pathFile.toString()});

		Resource resource = null;
		long r = 0;
	    InputStream is=null;

		try {
			resource = new UrlResource(pathFile.toUri());
	        is = resource.getInputStream();
	        r = resource.contentLength();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(!resource.exists() && !resource.isReadable()) {
			throw new RuntimeException("Error no se pudo cargar el archivo: " + nameFile);
		}


		return ResponseEntity.ok().contentLength(r)
                .contentType(MediaType.parseMediaType("application/pdf"))
                .body(new InputStreamResource(is));
	}
	
	@GetMapping(value="/report/structure", produces="application/json;charset=UTF-8")
	public ResponseEntity<String> reportStructure(@RequestParam(value="Report", required=true) String report) {

		AccessDataBase ADB = new AccessDataBase();

		String response = ADB.getStructure(report);

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	@GetMapping(value="/report/data", produces="application/json;charset=UTF-8")
	public ResponseEntity<?> reportData(@RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="Report", required=true) String report, @RequestParam(value="Find", required=true) String find,  @RequestParam(value="Params", required=false) String params) {

		AccessDataBase ADB = new AccessDataBase();

		String WH=find.replace("'","''").replace("\\|", "'").replace("¯", "'").replace("¦","%").replace("œ","+"),
			   PR=params != null ? params.replace("'","''").replace("\\|", "'").replace("¯", "'").replace("¦","%").replace("œ","+"):"";

		Map<String, Object> response = ADB.getReport(user_name, report, WH, PR);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}
	
	@PostMapping(value="/report/version", produces="application/json;charset=UTF-8", consumes="application/json;charset=UTF-8")
	public ResponseEntity<?> reportVersion(@RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="Report", required=true) String report, @RequestParam(value="version", required=true) String version, @RequestBody String json) {
		Map<String, Object> response = new HashMap<String, Object>();

		AccessDataBase ADB = new AccessDataBase();
		
		String ver = version;

		ver = ADB.saveVersion(report, user_name, version, json);

		response.put("VERSI", ver);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}
	
	@GetMapping(value="/report/versions", produces="application/json;charset=UTF-8")
	public ResponseEntity<?> reportVersions(@RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="Report", required=true) String report) {

		AccessDataBase ADB = new AccessDataBase();

		Map<String, Object> response = ADB.getVersions(user_name, report);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}
	
	@DeleteMapping(value="/report/version", produces="application/json;charset=UTF-8")
	public ResponseEntity<?> deleteVersion(@RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="Report", required=true) String report, @RequestParam(value="version", required=true) String version) {

		AccessDataBase ADB = new AccessDataBase();

		Map<String, Object> response = ADB.deleteVersion(user_name, report, version);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}
	
	@PostMapping(value="/catalogs/save/{option}", produces="application/json;charset=UTF-8", consumes="application/json;charset=UTF-8")
	public ResponseEntity<?> saveCatalogs(@PathVariable String option, @RequestParam(value="object", required=true) String object, @RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="folio", required=true) String folio, @RequestParam(value="params", required=false) String params, @RequestBody String json) {
		Map<String, Object> response = new HashMap<String, Object>();

		AccessDataBase ADB = new AccessDataBase();
		
		String fol = folio;
		String par=params != null ? params.replace("'","''").replace("\\|", "'").replace("¯", "'").replace("¦","%").replace("œ","+"):"";

		fol = ADB.prepareCatalogs(option, object, user_name, folio, par, json, uploads);

		response.put("FOLIO", fol);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}
	
	@DeleteMapping(value="/catalogs/delete", produces="application/json;charset=UTF-8")
	public ResponseEntity<?> deleteCatalogs(@RequestParam(value="object", required=true) String object, @RequestParam(value="user_name", required=true) String user_name, @RequestParam(value="folio", required=true) String folio) {

		AccessDataBase ADB = new AccessDataBase();

		Map<String, Object> response = ADB.deleteCatalogs(object, user_name, folio);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}
}

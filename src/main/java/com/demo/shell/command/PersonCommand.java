package com.demo.shell.command;

import java.io.*;
import java.util.List;
import java.util.Optional;

import com.demo.shell.entity.Persons;
import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.demo.shell.entity.Person;
import com.demo.shell.repo.PersonRepository;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

@ShellComponent
public class PersonCommand {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PersonCommand.class);

	private final PersonRepository repository;
	
	public PersonCommand(PersonRepository repository) {
		super();
		this.repository = repository;
	}
	
    @ShellMethod("Add a person [firstName, surname] returns id")
    long add(String firstName, String surname) {
        logger.debug("In add with firstName={} surname={}", firstName, surname);
    	Person personSaved = this.repository.save(Person.builder()
													.firstName(firstName)
													.surname(surname)
													.build());
        long id = personSaved.getId();
        logger.debug("Out add with {}", id);
    	return id;
    }
    
    @ShellMethod("Edit a person [id, firstName, surname]")
    String edit(long id, String firstName, String surname) {
        logger.debug("In edit with id={} firstName={} surname={}", id, firstName, surname);
    	String message = String.format("Person id %s does not exist", id);
		Optional<Person> personOpt = this.repository.findById(id);
        if (personOpt.isPresent()) {
        	Person person = personOpt.get();
        	person.setFirstName(firstName);
			person.setSurname(surname);
            this.repository.save(person);
			message = "Edit success";
		}
        logger.debug("Out edit with {}", message);
    	return message;
    }
    
    @ShellMethod("Delete a person [id]")
    String delete(long id) {
        logger.debug("In delete with {}", id);
    	String message = "Delete success";
        try {
            this.repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
			message = String.format("Person id %s does not exist", id);
		}
        logger.debug("Out delete with {}", message);
    	return message;
    }
    
    @ShellMethod("Count Number of Persons")
    long count() {
        logger.debug("In count");
        long count = this.repository.count();
        logger.debug("Out count with {}", count);
        return count;
    }
    
    @ShellMethod("List Persons")
    List<Person> list() {
        logger.debug("In list");
	    List<Person> list = this.repository.findAll();
        logger.debug("Out list with {}", list.size());
        return list;
    }

    @ShellMethod("Add persons from xml file [xmlFilePath] - Eg. C:/dir/fie.xml, Content eg. <persons><person><firstName></firstName><surname></surname></person></persons>")
    String addXmlFile(String xmlFilePath) {
        logger.debug("In addXmlFile with {}", xmlFilePath);
        String result;
        try {
            Unmarshaller jaxbUnmarshaller = JAXBContext.newInstance(Persons.class).createUnmarshaller();
            Persons persons = (Persons) jaxbUnmarshaller.unmarshal(this.newFileInputStream(xmlFilePath));
            List<Person> personsSaved = this.repository.saveAll(persons.getPersons());
            result = String.format("%s person(s) added", personsSaved.size());
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            result = "File not found";
        } catch (Exception e) {
            logger.debug("Error addXmlFile with {}", xmlFilePath);
            logger.error(e.getMessage(), e);
            result = "Error processing xml file";
        }
        logger.debug("Out addXmlFile with {}", result);
        return result;
    }

    InputStream newFileInputStream(String path) throws Exception {
        return new FileInputStream(new File(path));
    }

    @ShellMethod("Add a person from xml text [xmlText] - Eg. <persons><person><firstName>name</firstName><surname>surname</surname></person></persons>")
    String addXmlText(String xmlText) {
        logger.debug("In addXmlText with {}", xmlText);
        String result = "Error processing xml";
        try {
            Unmarshaller jaxbUnmarshaller = JAXBContext.newInstance(Persons.class).createUnmarshaller();
            Persons persons = (Persons) jaxbUnmarshaller.unmarshal(new StringReader(xmlText));
            List<Person> personsSaved = this.repository.saveAll(persons.getPersons());
            result = String.format("%s person(s) added", personsSaved.size());
        } catch (Exception e) {
            logger.debug("Error addXmlText with {}", xmlText);
            logger.error(e.getMessage(), e);
        }
        logger.debug("Out addXmlText with {}", result);
        return result;
    }
}
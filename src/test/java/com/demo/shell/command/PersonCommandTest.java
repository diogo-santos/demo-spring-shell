package com.demo.shell.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.shell.jline.InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED;
import static org.springframework.shell.jline.ScriptShellApplicationRunner.SPRING_SHELL_SCRIPT_ENABLED;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.demo.shell.entity.Person;
import com.demo.shell.repo.PersonRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {SPRING_SHELL_INTERACTIVE_ENABLED+"=false",SPRING_SHELL_SCRIPT_ENABLED+"=false"})
public class PersonCommandTest {
	@Autowired
	private PersonRepository repository;
	@Autowired
	private PersonCommand cmd;

	@Test
	public void addTest() {
		long result = this.cmd.add("firstAdd", "lastAdd");
		List<Person> personList = this.repository.findAll();
		assertThat(personList).extracting(Person::getId).contains(result);
		assertThat(personList).extracting(Person::getFirstName).contains("firstAdd");
		assertThat(personList).extracting(Person::getSurname).contains("lastAdd");
	}
	
	@Test
	public void editTest() {
		long id = this.repository.save(Person.builder()
										.firstName("test")
										.surname("test").build())
								 .getId();
		this.cmd.edit(id, "firstEdit", "lastEdit");
		List<Person> personList = this.repository.findAll();
		assertThat(personList).extracting(Person::getId).contains(id);
		assertThat(personList).extracting(Person::getFirstName).contains("firstEdit");
		assertThat(personList).extracting(Person::getSurname).contains("lastEdit");
	}
	
	@Test
	public void deleteTest() {
		long id = this.repository.save(Person.builder()
										.firstName("testDelete")
										.surname("testDelete").build())
								 .getId();
		this.cmd.delete(id);
		List<Person> personList = this.repository.findAll();
		assertThat(personList).extracting(Person::getId).doesNotContain(id);
	}
	
	@Test
	public void countTest() {
		long count = this.cmd.count();
		assertEquals(repository.count() , count);
	}
	
	@Test
	public void listTest() {
		this.repository.save(Person.builder().firstName("firstList1").surname("lastList1").build());
		this.cmd.add("firstList2","lastList2");
		List<Person> personList = this.cmd.list();
		assertThat(personList).extracting(Person::getFirstName).contains("firstList1","firstList2");
		assertThat(personList).extracting(Person::getSurname).contains("lastList1","lastList2");
	}

	@Test
	public void editNotExistTest() {
		String result = this.cmd.edit(-1000000, "test", "test");
		assertNotEquals("Edit success", result);
	}

	@Test
	public void deleteNotExistTest() {
		String result = this.cmd.delete(-1000000);
		assertNotEquals("Delete success", result);
	}

	@Test
	public void addXmlFileTest() throws Exception {
		String xml = "<person>" +
						"<firstName>firstXmlFile</firstName>" +
						"<surname>lastXmlFile</surname>" +
					 "</person>";
		InputStream is = new ByteArrayInputStream(xml.getBytes());

		PersonCommand cmdSpy = Mockito.spy(this.cmd);
		doReturn(is)
				.when(cmdSpy)
				.newFileInputStream(anyString());

		long id = cmdSpy.addXmlFile("person.xml");

		List<Person> personList = this.repository.findAll();
		assertThat(personList).extracting(Person::getId).contains(id);
		assertThat(personList).extracting(Person::getFirstName).contains("firstXmlFile");
		assertThat(personList).extracting(Person::getSurname).contains("lastXmlFile");
	}

	@Test
	public void addXmlTextTest() {
		String xml = "<person>" +
						"<firstName>firstXml</firstName>" +
						"<surname>lastXml</surname>" +
					 "</person>";
		long id = this.cmd.addXmlText(xml);

		List<Person> personList = this.repository.findAll();
		assertThat(personList).extracting(Person::getId).contains(id);
		assertThat(personList).extracting(Person::getFirstName).contains("firstXml");
		assertThat(personList).extracting(Person::getSurname).contains("lastXml");
	}

	@Test
	public void addXmlTextWhenErrorTest() {
		String xml = "<person>" +
						"<firstName>firstXml" +
						"<surname>lastXml";
		long id = this.cmd.addXmlText(xml);
		assertEquals(0, id);
	}

	@Test
	public void addXmlWhenFileNotExistsTest() {
		long id = this.cmd.addXmlFile("");
		assertEquals(0, id);
	}
}
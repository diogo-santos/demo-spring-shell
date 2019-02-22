package com.demo.shell.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.ScriptShellApplicationRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.demo.shell.entity.Person;
import com.demo.shell.repo.PersonRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED + "=false",
	    						ScriptShellApplicationRunner.SPRING_SHELL_SCRIPT_ENABLED + "=false"})
public class PersonCommandTest {
	@Autowired
	private PersonRepository repository;
	@Autowired
	private PersonCommand cmd;

	@Before
	public void setup() {
		cmd = new PersonCommand(repository);
	}

	@Test
	public void addTest() {
		long result = cmd.add("firstAdd", "lastAdd");
		Assert.isInstanceOf(Long.class, result);
		List<Person> personList = repository.findAll();
		assertThat(personList).extracting(Person::getId).contains(result);
		assertThat(personList).extracting(Person::getFirstName).contains("firstAdd");
		assertThat(personList).extracting(Person::getSurname).contains("lastAdd");
		
		repository.deleteById(result);
	}
	
	@Test
	public void editTest() {
		Long id = repository.save(Person.builder().firstName("test").surname("test").build())
							.getId();
		cmd.edit(id, "firstEdit", "lastEdit");
		List<Person> personList = repository.findAll();
		assertThat(personList).extracting(Person::getId).contains(id);
		assertThat(personList).extracting(Person::getFirstName).contains("firstEdit");
		assertThat(personList).extracting(Person::getSurname).contains("lastEdit");
		
		repository.deleteById(id);
	}
	
	@Test
	public void deleteTest() {
		Long id = repository.save(Person.builder().firstName("testDelete").surname("testDelete").build())
							.getId();
		cmd.delete(id);
		List<Person> personList = repository.findAll();
		assertThat(personList).extracting(Person::getId).doesNotContain(id);
	}
	
	@Test
	public void countTest() {
		repository.save(Person.builder().build());
		long count = cmd.count();
		assertThat(count == 1);
	}
	
	@Test
	public void listTest() {
		repository.save(Person.builder().firstName("testList").surname("testList").build());
		List<Person> personList = cmd.list();
		assertThat(personList).extracting(Person::getFirstName).contains("testList");
		assertThat(personList).extracting(Person::getSurname).contains("testList");
	}

	@Test
	public void editNotExistTest() {
		String result = cmd.edit(-1000000, "test", "test");
		assertFalse("Edit success".equals(result));
	}

	@Test
	public void deleteNotExistTest() {
		String result = cmd.delete(-1000000);
		assertFalse("Delete success".equals(result));
	}

	@Test
	public void addXmlTextTest() {
		String xml = "<person>" +
						"<firstName>firstXml</firstName>" +
						"<surname>lastXml</surname>" +
					 "</person>";
		long id = cmd.addXmlText(xml);

		List<Person> personList = repository.findAll();
		assertThat(personList).extracting(Person::getId).contains(id);
		assertThat(personList).extracting(Person::getFirstName).contains("firstXml");
		assertThat(personList).extracting(Person::getSurname).contains("lastXml");

		repository.deleteById(id);
	}

	@Test
	public void addXmlTextErrorTest() {
		String xml = "<person>" +
						"<firstName>firstXml" +
						"<surname>lastXml";
		long id = cmd.addXmlText(xml);
		assertEquals(0, id);
	}

	@Test
	public void addXmlFileNotExistsTest() {
		long id = cmd.addXmlFile("");
		assertEquals(0, id);
	}
}
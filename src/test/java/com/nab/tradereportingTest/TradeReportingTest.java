package com.nab.tradereportingTest;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.nab.tradereporting.TradeReportingEngine;

import junit.framework.TestCase;

/**
 * Unit test for Trade Reporting Test
 */
public class TradeReportingTest extends TestCase {


	TradeReportingEngine e = new TradeReportingEngine();
	
	
	
	
	/**
	 * Test to check the buyer party reference and seller party reference values anagrams
	 */
	public void testCheckAnagrams(){
		String s1="EMU_BANK";
		String s2="LEFT_BANK";
		boolean flag = e.checkAnagram(s1, s2);
		assertEquals(false, flag);
	}
	/**
	 * Test to check XML reading for event0.xml file using xpath
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public void testReadXML() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder=factory.newDocumentBuilder();
		Document doc = null;
		File eventfile=new File("src/main/files/event0.xml");
		StringBuilder sb = new StringBuilder();
		sb.append("LEFT_BANK").append(",");
		sb.append("EMU_BANK").append(",");
		sb.append("100.00").append(",");
		sb.append("AUD");
		String expected = sb.toString();
		String actual=e.readXML(eventfile, doc, builder);		
		assertEquals(expected,actual);
	}
	
	
	
	
}

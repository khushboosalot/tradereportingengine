package com.nab.tradereporting;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * Trade Reporting Engine
 *
 */
public class TradeReportingEngine {

	private static final String CSV_SEPARATOR = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String BUYER_PARTY = "buyerPartyReference";
	private static final String SELLER_PARTY = "sellerPartyReference";
	private static final String CURRENCY = "currency";
	private static final String AMOUNT = "amount";
	private static final String EQUITY_PREMIUM = "equityPremium";
	private static final String PAYMENT_AMT = "paymentAmount";
	private static final String CSV_HEADER_COL1 = "buyer_party";;
	private static final String CSV_HEADER_COL2 = "seller_party";;
	private static final String CSV_HEADER_COL3 = "premium_amount";
	private static final String CSV_HEADER_COL4 = "premium_currency";
	private static final String HREF_ATTR="href";
	private static String relativeDir = "src/main/files";
	private String xPathExpr = "/requestConfirmation/trade/varianceOptionTransactionSupplement[(sellerPartyReference[@href='EMU_BANK'] and equityPremium/paymentAmount/currency='AUD') or (sellerPartyReference[@href='BISON_BANK'] and equityPremium/paymentAmount/currency='USD')]";
	private ArrayList<String> csvHeader = new ArrayList<String>(
			Arrays.asList(CSV_HEADER_COL1, CSV_SEPARATOR,CSV_HEADER_COL2,CSV_SEPARATOR, CSV_HEADER_COL3,CSV_SEPARATOR, CSV_HEADER_COL4));
	private static String outputFile = "OutputTradeDetails.csv";

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc = null;

		try {
			builder = factory.newDocumentBuilder();

			// Directory where the xml files are located
			File dir = new File(relativeDir);

			// Create a FileFilter that matches .xml files
			FileFilter filter = (File file) -> file.isFile() && file.getName().endsWith(".xml");

			// Get pathnames of matching files
			File[] paths = dir.listFiles(filter);

			if (paths != null && paths.length >= 1) {
				TradeReportingEngine tradeReporting = new TradeReportingEngine();
				List<String> list = new ArrayList<String>();
				for (File eventfile : paths) {
					// Read each xml file and get the data
					String str = tradeReporting.readXML(eventfile, doc, builder);					
					if(str!=null && !str.isEmpty()){
						list.add(str);
					}
				}
				// Write xml data values to csv
				tradeReporting.writeContentToCsv(list);				
				System.out.println("Please check event report at location: "+relativeDir+"/"+outputFile);
			} else {
				System.out.println("xml files are not available.");
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method reads node attributes and data from XML file using XPath
	 * 
	 * @param File
	 *            xml file
	 * @param Document
	 *            XML document
	 * @param DocumentBuilder
	 *            DOM instance of XML file
	 * @return String XML data details in String
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * 
	 */
	public String readXML(File eventfile, Document doc, DocumentBuilder builder)
			throws SAXException, IOException, XPathExpressionException {

		StringBuilder str = new StringBuilder();
		doc = builder.parse(eventfile);
		// Create XPathFactory object
		XPathFactory xpathFactory = XPathFactory.newInstance();
		// Create XPath object
		XPath xpath = xpathFactory.newXPath();
		/* xPathExpr is based on following condition - (The seller_party is EMU_BANK
		 and the premium_currency is AUD) or (the seller_party is BISON_BANK
		 and the premium_currency is USD)*/
		XPathExpression expr = xpath.compile(xPathExpr);

		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		// System.out.println(nodes.getLength());
		String strbuyer_party = null, strseller_party = null, strCurrency = null, strAmt = null;

		for (int i = 0; i < nodes.getLength(); i++) {
			Element el = (Element) nodes.item(i);
			NodeList children_VOTS = el.getChildNodes();

			for (int j = 0; j < children_VOTS.getLength(); j++) {
				Node child_VOTS = children_VOTS.item(j);

				if (child_VOTS.getNodeName().equals(BUYER_PARTY)) {
					strbuyer_party = child_VOTS.getAttributes().getNamedItem(HREF_ATTR).getTextContent();

				} else if (child_VOTS.getNodeName().equals(SELLER_PARTY)) {
					strseller_party = child_VOTS.getAttributes().getNamedItem(HREF_ATTR).getTextContent();

				} else {
					if (child_VOTS.getNodeType() != Node.TEXT_NODE && child_VOTS.getNodeName().equals(EQUITY_PREMIUM)) {
						NodeList children_EquityPremium = (NodeList) xpath.evaluate(PAYMENT_AMT, child_VOTS,
								XPathConstants.NODESET);

						for (int k = 0; k < children_EquityPremium.getLength(); k++) {
							Node child_PaymentAmt = children_EquityPremium.item(k);

							NodeList child_PA = (NodeList) xpath.evaluate(CURRENCY, child_PaymentAmt,
									XPathConstants.NODESET);
							strCurrency = child_PA.item(0).getTextContent();

							child_PA = (NodeList) xpath.evaluate(AMOUNT, child_PaymentAmt, XPathConstants.NODESET);
							strAmt = child_PA.item(0).getTextContent();

						}

					}
				}

			}

			// Check whether buy and seller party are not anagrams
			if (!checkAnagram(strbuyer_party, strseller_party)) {

				str.append(strbuyer_party).append(CSV_SEPARATOR);
				str.append(strseller_party).append(CSV_SEPARATOR);
				str.append(strAmt).append(CSV_SEPARATOR);
				str.append(strCurrency);

			}
		}

		return str.toString();
	}

	/**
	 * Check whether two strings are anagrams This method returns false if two
	 * strings are not anagrams, else this will return true.
	 * 
	 * @param strBuyerParty
	 * @param strSellerParty
	 * @return boolean
	 */
	public boolean checkAnagram(String strBuyerParty, String strSellerParty) {

		boolean isAnagram = false;

		if (strBuyerParty.length() == strSellerParty.length()) {
			return Arrays.equals(strBuyerParty.chars().sorted().toArray(), strSellerParty.chars().sorted().toArray());
		}
		return isAnagram;
	}

	/**
	 * Write List of items in CSV
	 * 
	 * @param lstCSVItems
	 *            List of lines of CSV file
	 * @throws IOException
	 */
	public void writeContentToCsv(List<String> lstCSVItems) throws IOException {
		FileWriter writer = new FileWriter(relativeDir+"/"+outputFile);

		// Write CSV header values
		for (String value : csvHeader) {
			writer.append(value);
		}
		
		writer.append(NEW_LINE_SEPARATOR);

		// Write CSV data values
		for (String line : lstCSVItems) {
			writer.append(line).append(NEW_LINE_SEPARATOR);
		}
		writer.flush();
		writer.close();

	}

	

}

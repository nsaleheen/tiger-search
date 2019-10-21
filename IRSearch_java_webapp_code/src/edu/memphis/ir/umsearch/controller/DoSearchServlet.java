package edu.memphis.ir.umsearch.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.memphis.ir.umsearch.domain.Node;


@WebServlet("/search.do")
public class DoSearchServlet extends HttpServlet {
	static Map<String, List<Node>> wordToDocs;
	static Map<String, Double> idf;
	static Map<String, Double> docLength;
	public static Map<Integer, String> indxToURL;

	static Map<String, Double> relevantDocs;

	static int nDocument = 10000;

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		InputStream inputInvertedIndex = getServletContext().getResourceAsStream("/WEB-INF/invertedIndexFile.txt");
		InputStream inputURLs = getServletContext().getResourceAsStream("/WEB-INF/URLs.txt");

		doInit(inputInvertedIndex, inputURLs);

		HttpSession session = request.getSession();
		String queryString=request.getParameter("searchText");

		System.out.print("Query string: "+queryString);
		String[] toks = queryString.split(" ");

        List<Node> R = getListDoc(toks);

        List<String> res = new ArrayList<String>();
        for (int i=0; i<R.size(); i++) {
        	String cntTxt = R.get(i).str;
        	cntTxt = cntTxt.substring(0, cntTxt.length()-4);
        	String url = indxToURL.get(Integer.parseInt(cntTxt));
//        	res.add(url + " : "+R.get(i).count);
        	res.add(url);
//            System.out.println(R.get(i).str + " : "+R.get(i).count);
        }

		request.setAttribute("result", res);
		String viewPath = "/WEB-INF/jsp/search.jsp";
		RequestDispatcher view = request.getRequestDispatcher(viewPath);
		view.forward(request, response);
	}

	private void doInit(InputStream inputInvertedIndex, InputStream inputURLs) {
		parseFile(inputInvertedIndex);
		indexToURL(inputURLs);

		System.out.println("Complete parsing");
		computeDocumentLength();
	}

	private static void indexToURL(InputStream input) {
		indxToURL = new HashMap<Integer, String>();
		try {
			Scanner sc = new Scanner(input);
			while (sc.hasNext()) {
				String s = sc.nextLine();
				String[] toks = s.split(":");
				String index = toks[0];
				String url = toks[1]+":"+toks[2];
				indxToURL.put(Integer.parseInt(index), url);
			}
		} catch (Exception e) {
		}
	}

	private static void computeDocumentLength() {
		docLength = new HashMap<String, Double>();
		for(String trm : wordToDocs.keySet()) {
			double i = idf.get(trm);
			for(Node nd : wordToDocs.get(trm)) {
				double cnt = nd.count;
				if(!docLength.containsKey(nd.str))
					docLength.put(nd.str, 0.0);
				docLength.put(nd.str, docLength.get(nd.str)+(i*cnt)*(i*cnt));
			}
		}
		for(String doc : docLength.keySet()) {
			docLength.put(doc, Math.sqrt(docLength.get(doc)));
		}
	}

	public static List<Node> getListDoc(String[] toks) {
		List<Node> R = new ArrayList<Node>();
		relevantDocs = new HashMap<String, Double>();
		for(String tok:toks){
			List<Node> docs = wordToDocs.get(tok);

			if(docs != null)
				for(Node nd : wordToDocs.get(tok))  {
					if(!relevantDocs.containsKey(nd.str)){
						relevantDocs.put(nd.str, 0.0);
					}
					relevantDocs.put(nd.str, relevantDocs.get(nd.str)+idf.get(tok)*idf.get(tok)*nd.count);
				}
		}
		for(String d : relevantDocs.keySet()) {
			R.add(new Node(d, relevantDocs.get(d)/docLength.get(d)));
		}

		for(int i=0; i<R.size(); i++) {
			for(int j=i+1; j<R.size(); j++){
				if(R.get(i).count < R.get(j).count) {
					Node nd = R.get(i);
					R.set(i, R.get(j));
					R.set(j, nd);
				}
			}
		}
		return R;
	}

	static void parseFile(InputStream input) {

		wordToDocs = new HashMap<String, List<Node>>();
		idf = new HashMap<String, Double>();
		try {
			Scanner sc = new Scanner(input);
			while (sc.hasNext()) {
				String s = sc.nextLine();
				String[] toks = s.split(":");
				String word = toks[0].split(",")[0];
				String cntS = toks[0].split(",")[1];
				int cnt = Integer.parseInt(cntS);



				String[] docs = toks[1].split(";");
				List<Node> docList = new ArrayList<Node>();
				for (String doc : docs) {
					Node nd = new Node(doc.split(",")[0], Integer.parseInt(doc.split(",")[1]));

					docList.add(nd);
				}
				wordToDocs.put(word, docList);
				idf.put(word, cnt*1.0/nDocument);
			}
		} catch (Exception e) {

		}
	}
}

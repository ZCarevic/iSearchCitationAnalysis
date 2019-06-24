import groovy.sql.Sql
class Isearch {

	static url  = "jdbc:mysql://localhost:3306/isearch-references"
	static user = "youruser"
	static pass = "yourpass"
	static driver = "com.mysql.jdbc.Driver"
	static main(args) {
		init();
	}
	def init(arxivid){
	// init the script with a valid ISearch-record-ID
	this.getRowForId(arxivid);
		
	}
	//estimate how often a reference is cited in total
	def int getDF(arxivid){
		def sql = Sql.newInstance(url, user,pass,driver)
		def query = 'select count(arxivURI) from `isearch-references`.`refs`  where internalReferenceId=\"'+arxivid+'\" ';
		def df;
		sql.eachRow(query){
			 df = it."count(arxivURI)"			 
		}
		sql.close();
		return df;
	}
	

	//estimate how often a reference is co-cited with the seed
	def getRowForId(arxivid){
		def sql = Sql.newInstance(url, user,pass,driver)
		def query = 'SELECT internalReferenceId,count(arxivURI) FROM `isearch-references`.`refs` where arxivURI in ('+
		'SELECT arxivURI FROM `isearch-references`.`refs` where internalReferenceId = \"'+arxivid+'\")'+
		'and internalReferenceId is not null group by internalReferenceId';
		
		def res = sql.rows(query);
		def resultList = [];
		//remove existing files
		new File("your/output/directory").delete()		
		
		File csvFile = new File("your/output/directory/file.csv")
		
		csvFile << "uri;tf;df;log_tf;log_df;tfidf\n"
				
		sql.eachRow(query){
			String uri = it."internalReferenceId"
			int tf = it."count(arxivURI)".toInteger()
			int df = getDF(uri);			
					if(tf > 1 && df > 1){						
						float log_idf = Math.log10(100000.div(df)) 
						float log_tf = Math.log10(tf)
						float tfidf = log_tf * log_idf
						csvFile << "${uri};${tf};${df};${log_tf};${log_idf};${tfidf}\n"
						println "${uri};${tf};${df};${log_tf};${log_idf};${tfidf}"						
					}			
				}			
		sql.close();		
	}
}

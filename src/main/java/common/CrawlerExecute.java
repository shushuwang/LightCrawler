package common;

import java.util.List;

public abstract class CrawlerExecute {
	
	public abstract List<String> execute(String taskDetail) throws Exception;
	public abstract int changeMsgCode();
	
	public abstract String getResultMark();
	
}

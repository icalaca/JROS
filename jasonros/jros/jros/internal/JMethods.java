package jros.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class JMethods {
private static ConcurrentHashMap<String,ROSConnection> agMap = new ConcurrentHashMap<String,ROSConnection>();
	
	public static boolean rosConfig(String agName, Agent ag, String rosIP, String rosPort,String remoteAgName) throws InterruptedException{
		System.out.println("criou");
		ROSConnection rc = agMap.get(ag);
		if(rc == null){
			rc = new ROSConnection(ag,remoteAgName);
			agMap.put(agName, rc);
		}
		return rc.rosConfig(rosIP, rosPort);
	}
	
	public static boolean addPubGenericTopic(String ag, String topicName, String msgType, String className, Unifier un, Term[] terms) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		ROSConnection rc = agMap.get(ag);
		return rc.addPubGenericTopic(topicName, msgType, className, un, terms);
	}
	
	public static boolean addSubGenericTopic(String ag, String topicName, String msgType, String className, Unifier un, Term[] terms) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		ROSConnection rc = agMap.get(ag);
		return rc.addSubGenericTopic(topicName, msgType, className, un, terms);
	}
	
	public static boolean listenPerceptions(String ag){
		ROSConnection rc = agMap.get(ag);
		return rc.listenPerceptions();
	}
	
	public static boolean sendAction(String ag, String action, List<String> parameters) throws InterruptedException{
		ROSConnection rc = agMap.get(ag);
		return rc.sendAction(ag.toString(), action, parameters);
	}
	
	public static boolean createSubNode(String ag, String nodeName) throws InterruptedException{
		ROSConnection rc = agMap.get(ag);
		return rc.createSubNode(nodeName);
	}
	
	public static boolean createPubNode(String ag, String nodeName, long pRate) throws InterruptedException{
		ROSConnection rc = agMap.get(ag);
		return rc.createPubNode(nodeName, pRate);
	}
	
	public static boolean addSubTopic(String ag, String topicName, String msgType){
		ROSConnection rc = agMap.get(ag);
		return rc.addSubTopic(topicName, msgType);
	}
	
	public static boolean addPubTopic(String ag, String topicName, String msgType, Object data){
		ROSConnection rc = agMap.get(ag);
		return rc.addPubTopic(topicName, msgType, data);
	}
	
	public static boolean searchLT(String ag, String topicName, Object value){
		ROSConnection rc = agMap.get(ag);
		if(rc.subExists()){
			return rc.getListenerInstance().searchLT(topicName, value);
		}else{
			System.out.println("Subscriber node doesn't exists!");
			return false;
		}
	}
	
	public static boolean searchBT(String ag, String topicName, Object value){
		ROSConnection rc = agMap.get(ag);
		if(rc.subExists()){
			return rc.getListenerInstance().searchBT(topicName, value);
		}else{
			System.out.println("Subscriber node doesn't exists!");
			return false;
		}
	}
	
	public static boolean searchExact(String ag, String topicName, Object value){
		ROSConnection rc = agMap.get(ag);
		if(rc.subExists())
			return rc.getListenerInstance().searchExact(topicName, value);
		else{
			System.out.println("Subscriber node doesn't exists!");
			return false;
		}
	}
	
	public static boolean clearDataList(String ag, String topicName){
		ROSConnection rc = agMap.get(ag);
		if(rc.subExists()){
			rc.getListenerInstance().clearDataList(topicName);
			return true;
		}
		return false;
	}
	
	public static Object getTopicData(String ag, String topicName) throws InterruptedException{
		ROSConnection rc = agMap.get(ag);
		if(rc.subExists()){
			//Object data = null;
			Object data = rc.getListenerInstance().searchTopic(topicName);
			//timeoutTimer(getTopicTimeout);
//			while(data == null)
//				data = rosconn.getListenerInstance().searchTopic(topicName);
			/*if(timeout){
				System.out.println("getTopicData: timeout!");
				timeout = false;
			}else timer.cancel();*/
			if(data != null)
				return data;
		}else
			System.out.println("Subscriber node doesn't exists!");
			//return null;
		return null;
	}
	
	public static boolean setTopicData(String ag, String nodeName, String topicName, Object data) throws InterruptedException{
		ROSConnection rc = agMap.get(ag);
		if(rc.pubExists(topicName)){
		//rosconn.getTalkerInstance().setTopicData(topicName, msgType, data);
		ArrayList<JasonTalker> l = rc.getTalkerList();
		for(JasonTalker jt : l){//nodeName -> nodo que ira publicar o dado
			if(jt.getNodeName().equals(nodeName)){
			//	Object topicData = getTopicData(topicName);
				//timeoutTimer(setTopicTimeout);
			//	while(!topicData.equals(data))
			//		topicData = getTopicData(topicName);
				/*if(timeout){
					System.out.println("setTopicData: timeout!");
					timeout = false;
					return false;
				}else timer.cancel();*/
				return jt.setTopicData(topicName, data);
			
			}
		}
		}
		return false;//topico nao existe ou nodo nao existe
	}
	
	public void closeROSConn(String ag){
		ROSConnection rc = agMap.get(ag);
		rc.shutdown();
		agMap.remove(ag);
	}

}

package jros.internal;


import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import jason.asSemantics.Agent;

public class JMethods {
private static ConcurrentHashMap<Agent,ROSConnection> agMap = new ConcurrentHashMap<Agent,ROSConnection>();
	
	public static boolean rosConfig(Agent ag, String rosIP, String rosPort) throws InterruptedException{
		ROSConnection rc = agMap.get(ag);
		if(rc == null){
			rc = new ROSConnection();
			agMap.put(ag, rc);
		}
		return rc.rosConfig(rosIP, rosPort);
	}
	
	public static boolean addPubGenericTopic(Agent ag, String topicName, String msgType, String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		ROSConnection rc = agMap.get(ag);
		return rc.addPubGenericTopic(topicName, msgType, className);
	}
	
	public static boolean createSubNode(Agent ag, String nodeName) throws InterruptedException{
		ROSConnection rc = agMap.get(ag);
		return rc.createSubNode(nodeName);
	}
	
	public static boolean createPubNode(Agent ag, String nodeName, long pRate) throws InterruptedException{
		ROSConnection rc = agMap.get(ag);
		return rc.createPubNode(nodeName, pRate);
	}
	
	public static boolean addSubTopic(Agent ag, String topicName, String msgType){
		ROSConnection rc = agMap.get(ag);
		return rc.addSubTopic(topicName, msgType);
	}
	
	public static boolean addPubTopic(Agent ag, String topicName, String msgType, Object data){
		ROSConnection rc = agMap.get(ag);
		return rc.addPubTopic(topicName, msgType, data);
	}
	
	public static boolean searchLT(Agent ag, String topicName, Object value){
		ROSConnection rc = agMap.get(ag);
		if(rc.subExists()){
			return rc.getListenerInstance().searchLT(topicName, value);
		}else{
			System.out.println("Subscriber node doesn't exists!");
			return false;
		}
	}
	
	public static boolean searchBT(Agent ag, String topicName, Object value){
		ROSConnection rc = agMap.get(ag);
		if(rc.subExists()){
			return rc.getListenerInstance().searchBT(topicName, value);
		}else{
			System.out.println("Subscriber node doesn't exists!");
			return false;
		}
	}
	
	public static boolean searchExact(Agent ag, String topicName, Object value){
		ROSConnection rc = agMap.get(ag);
		if(rc.subExists())
			return rc.getListenerInstance().searchExact(topicName, value);
		else{
			System.out.println("Subscriber node doesn't exists!");
			return false;
		}
	}
	
	public static boolean clearDataList(Agent ag, String topicName){
		ROSConnection rc = agMap.get(ag);
		if(rc.subExists()){
			rc.getListenerInstance().clearDataList(topicName);
			return true;
		}
		return false;
	}
	
	public static Object getTopicData(Agent ag, String topicName) throws InterruptedException{
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
	
	public static boolean setTopicData(Agent ag, String nodeName, String topicName, Object data) throws InterruptedException{
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
	
	public void closeROSConn(Agent ag){
		ROSConnection rc = agMap.get(ag);
		rc.shutdown();
		agMap.remove(ag);
	}

}
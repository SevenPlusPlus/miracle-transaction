package com.miracle.common.transaction.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.miracle.common.transaction.exception.TccException;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import lombok.extern.slf4j.Slf4j;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianFactory;

@Slf4j
public class HessianSerializer implements ObjectSerializer{

	private static Objenesis objenesis = new ObjenesisStd(true);
	
	@Override
	public byte[] serialize(Object obj) throws TccException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();  
        Hessian2Output ho2 = new Hessian2Output(os);  
        
        try {    
            ho2.writeObject(obj);    
            try{
            	ho2.close();
            	ho2 = null;
            }
            catch(IOException e)
            {
            	log.error("serializeHessian, close ho2 failed:" + e);
            }
            byte[] ret = os.toByteArray();
            try{
            	os.close();
            	os = null;
            }
            catch(IOException e)
            {
            	log.error("serializeHessian, close ho2 failed:" + e);
            }
            return ret;
        } catch (Exception e) {
        	log.error("Hessian serialize message failed.", e);
        	throw new TccException(e.getMessage(), e);
		} 
        finally
		{
        	try {
        		if(ho2 != null)
        		{
        			ho2.close();
        		}
        		if(os != null)
        		{
        			os.close();
        		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deSerialize(byte[] param, Class<T> clazz) throws TccException {
		HessianFactory hFactory = new HessianFactory();
   		ByteArrayInputStream is = new ByteArrayInputStream(param); 
   		Hessian2Input h2in =hFactory.createHessian2Input(is);  
   		
        try {
            T message = (T) objenesis.newInstance(clazz);
    		message= (T)h2in.readObject();
            return message;
        } catch (Exception e) {
        	log.error("Hessian deserialize message failed.", e);
            throw new TccException(e.getMessage(), e);
        } finally {
        	try {
				h2in.close();
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}

}

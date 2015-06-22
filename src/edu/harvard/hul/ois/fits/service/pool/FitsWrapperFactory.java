package edu.harvard.hul.ois.fits.service.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.log4j.Logger;

public class FitsWrapperFactory extends BasePooledObjectFactory<FitsWrapper> {

    private static Logger LOG = Logger.getLogger(FitsWrapperFactory.class);

    @Override
    public FitsWrapper create() throws Exception {
        LOG.debug("Creating new FitsWrapper instance in pool");
        return new FitsWrapper();
    }

    @Override
    public PooledObject<FitsWrapper> wrap(FitsWrapper fitsWrapper){
        return new DefaultPooledObject<FitsWrapper>(fitsWrapper);
    }

    @Override
    public boolean validateObject(PooledObject<FitsWrapper> fitsWrapper){
        return fitsWrapper.getObject().isValid();
    }


}
package cn.xxt.commons.util;

/**
 * Created by Luke on 16/11/30.
 */

import java.io.InputStream;
import java.net.URL;

/**
 * <p>
 * Title: LoonFramework
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: LoonFramework
 * </p>
 * <p>
 * License: http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 *
 * @author chenpeng
 * @email��ceponline@yahoo.com.cn
 * @version 0.1
 */
abstract class AbstractEncode extends Encoding {

    abstract public boolean isUtf(String path);

    abstract public boolean isUtf(InputStream in);

    abstract public boolean isUtf(byte[] buffer);

    abstract public boolean isUtf(URL url);

}

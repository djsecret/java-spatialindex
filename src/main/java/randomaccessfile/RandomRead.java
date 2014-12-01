package randomaccessfile;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Read data from file at specific location use RandomAccessFile
 * Created by ubuntu on 14-11-29.
 */
public class RandomRead
{
    public static final int BUFFER_SIZE = 100;
    /**
     * 随机读取文件内容
     *
     * @param fileName 文件名
     */
    public static void readFileByRandomAccess(String fileName)
    {
        RandomAccessFile randomFile = null;
        try
        {
            System.out.println("readFileByRandomAccess:");
            randomFile = new RandomAccessFile(fileName, "r");
            long fileLength = randomFile.length();
            int beginIndex = (fileLength > 90) ? 1870 : 0;
            //randomFile.seek(beginIndex);
//            int templ = randomFile.readLine().length();
//            System.out.println("length:" + templ);
            byte[] buffer = new byte[BUFFER_SIZE];
            randomFile.seek(beginIndex);
            byte c = -1;
            boolean eol = false;
            int index = 0;

            while (!eol)
            {
                switch (c = (byte)randomFile.read())
                {
                    case -1:
                    case '\n':
                        eol = true;
                        break;
                    case '\r':
                        eol = true;
                        long cur = randomFile.getFilePointer();
                        if ((randomFile.read()) != '\n')
                        {
                            randomFile.seek(cur);
                        }
                        break;
                    default:
                        buffer[index++] = c;
                        break;
                }
            }
            System.out.println(index);
            System.out.println(new String(buffer,0,index));

        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (randomFile != null)
            {
                try
                {
                    randomFile.close();
                } catch (IOException ignored)
                {
                }
            }
        }
    }


    public static void main(String[] args)
    {
        RandomRead.readFileByRandomAccess("data.csv");
    }

}

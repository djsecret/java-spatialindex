import spatialindex.rtree.RTree;
import spatialindex.spatialindex.*;
import spatialindex.storagemanager.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

/**
 * read data from "data.cvs" and build r-tree
 * Created by ubuntu on 14-12-1.
 */
public class BuildTree
{
    public static final int BUFFER_SIZE = 100;
    public static final String DATEFORMAT_STRING = "yyyy／MM／dd hh:mm:ss";// the "/" is chinese!

    public static void buildTree(String fileName) throws IOException
    {
        /********************init R-tree**********************/
        // Create a disk based storage manager.
        PropertySet ps = new PropertySet();

        ps.setProperty("Overwrite", true);
        //overwrite the file if it exists.

        ps.setProperty("FileName", "tree");
        // .idx and .dat extensions will be added.

        ps.setProperty("PageSize", 4096);
        // specify the page size. Since the index may also contain user defined data
        // there is no way to know how big a single node may become. The storage manager
        // will use multiple pages per node if needed. Of course this will slow down performance.

        IStorageManager diskfile = new DiskStorageManager(ps);

        IBuffer file = new RandomEvictionsBuffer(diskfile, 10, false);

        // Create a new, empty, RTree with dimensionality 3, minimum load 70%, using "file" as
        // the StorageManager and the RSTAR splitting policy.
        PropertySet ps2 = new PropertySet();

        Double f = 0.7;
        ps2.setProperty("FillFactor", f);


        ps2.setProperty("IndexCapacity", 4);
        ps2.setProperty("LeafCapacity", 4);
        // Index capacity and leaf capacity may be different.

        ps2.setProperty("Dimension", 3);

        ISpatialIndex tree = new RTree(ps2, file);


        /********************insert into R-tree**********************/
        RandomAccessFile randomFile = null;

        System.out.println("build tree:");
        randomFile = new RandomAccessFile(fileName, "r");

        byte[] buffer = new byte[BUFFER_SIZE];


        int readNumber = 0;

        int id;
        double lng, lat;
        String datetime;
        double[] pointData = new double[3];

        while(readNumber < randomFile.length())
        {
            int index = readLine(randomFile, buffer);
            String line = new String(buffer,0,index);
            readNumber += index+1;
            StringTokenizer st = new StringTokenizer(line, ";");
            id = new Integer(st.nextToken());
            datetime = st.nextToken().trim();
            lng = new Double(st.nextToken());
            lat = new Double(st.nextToken());

            SimpleDateFormat myFormatter = new SimpleDateFormat(DATEFORMAT_STRING);
            java.util.Date date = null;
            try
            {
                date = myFormatter.parse(datetime);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            long time = 0;
            if (date != null)
            {
                time = date.getTime() / 1000;//second since January 1, 1970, 00:00:00 GMT
            }

            pointData[0] = time;
            pointData[1] = lng;
            pointData[2] = lat;


            Point point = new Point(pointData);

            String data = String.valueOf(readNumber);
            tree.insertData(data.getBytes(), point, id);

//            System.out.println(index);
            System.out.println(readNumber);
//            System.out.println(line);
            System.out.println("id:" + id + ",datetime:" + time + ",lng:" + lng + ",lat:" + lat);
        }
        randomFile.close();
        System.err.println(tree);
        Integer indexID = (Integer) ps2.getProperty("IndexIdentifier");
        System.err.println("Index ID: " + indexID);

        boolean ret = tree.isIndexValid();
        if (!ret) System.err.println("Structure is INVALID!");

        // flush all pending changes to persistent storage (needed since Java might not call finalize when JVM exits).
        tree.flush();

    }


    public static ISpatialIndex getTree()
    {
        // Create a disk based storage manager.
        PropertySet ps1 = new PropertySet();

        ps1.setProperty("FileName", "tree");
        // .idx and .dat extensions will be added.

        IStorageManager diskfile1 = null;
        try
        {
            diskfile1 = new DiskStorageManager(ps1);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        IBuffer file1 = new RandomEvictionsBuffer(diskfile1, 10, false);
        // applies a main memory random buffer on top of the persistent storage manager
        // (LRU buffer, etc can be created the same way).

        PropertySet ps2 = new PropertySet();

        // If we need to open an existing tree stored in the storage manager, we only
        // have to specify the index identifier as follows
        Integer i = new Integer(1); // INDEX_IDENTIFIER_GOES_HERE (suppose I know that in this case it is equal to 1);
        ps2.setProperty("IndexIdentifier", i);
        // this will try to locate and open an already existing r-tree index from file manager file.

        return new RTree(ps2, file1);
    }

    public static int readLine(RandomAccessFile randomFile,byte[] buffer) throws IOException
    {
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

        return index;
    }

    public static String readLine(RandomAccessFile randomFile) throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];
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

        return new String(buffer,0,index);
    }



    public static void main(String[] args)
    {
        try
        {
            BuildTree.buildTree("data.csv");

            ISpatialIndex tree = BuildTree.getTree();
            double[] data1 = {1417401382,71.12293,41.96888};
            double[] data2 = {1417401382,70.18544,41.74363};
            double[] data3 = {70.64253,41.26596,1417225399};
            double[] data4 = {70.64253,41.26596,123};

            Point point1 = new Point(data1);
            Point point2 = new Point(data2);
            Point point3 = new Point(data3);
            Point point4 = new Point(data4);

            MyVisitor vis = new MyVisitor();
            tree.intersectionQuery(point1,vis);
            tree.intersectionQuery(point2,vis);
            tree.intersectionQuery(point3,vis);
            tree.intersectionQuery(point4,vis);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}


class MyVisitor implements IVisitor
{
    public static final String FILENAME = "data.csv";

    public void visitNode(final INode n) {}

    public void visitData(final IData d)
    {
        System.out.print("query result:");
        System.out.println(d.getIdentifier());
        System.out.println(d.getShape().toString());

        byte[] bytes = d.getData();
        if(bytes != null)
        {
            int position = Integer.parseInt(new String(bytes));
            RandomAccessFile randomFile = null;
            try
            {
                System.out.println("readFileByRandomAccess:");
                randomFile = new RandomAccessFile(FILENAME, "r");

                randomFile.seek(position);

                System.out.println(BuildTree.readLine(randomFile));

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
        else
        {
            System.out.println("null");
        }
    }

}
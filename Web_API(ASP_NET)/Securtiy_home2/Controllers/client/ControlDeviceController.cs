using Securtiy_home2.Models;
using System;
using System.Data;
using System.Data.SqlClient;
using System.Net;
using System.Net.Http;
using System.Web.Http;
namespace Securtiy_home2.Controllers
{
    public class ControlDeviceController : ApiController
    {

        SqlConnection con = new SqlConnection(
            @"server=DESKTOP-IB1JUFQ\MSSQL2019;" +
            "database=Securicy_home;" +
            "Integrated Security=true;" +
            "Max Pool Size = 10000;");

        [Route("app/getDevice")]
        [HttpPost]
        public HttpResponseMessage PostPowerUsed([FromBody] Device device)
        {
            String sqlStr = "SELECT DISTINCT hd.deviceId, hd.deviceName, hd.deviceDetailId, hd.status " +
                            "From Home.device hd " +
                            "INNER JOIN Home.GroupDevice gd ON gd.deviceId = hd.deviceId " +
                            "WHERE gd.groupId = ( " +
                            "SELECT groupId " +
                            "FROM App.GroupAccount " +
                            "Where accountId = (" +
                            "SELECT accountId " +
                            "FROM App.Device " +
                            "WHERE accountId = '" + Encryption.unEncryption(device.accountToken) + "' and deviceId = '" +
                                Encryption.unEncryption(device.deviceId) + "' and status = 1) )";

            try
            {
                SqlDataAdapter da = new SqlDataAdapter(sqlStr, con);
                DataTable dt = new DataTable();
                da.Fill(dt);

                if (dt.Rows.Count == 0)
                {
                    return Request.CreateResponse(
                        HttpStatusCode.BadRequest,
                        new Error("Device error!", "This device is not access!"));
                }

                return Request.CreateResponse(HttpStatusCode.OK, new Success(1, dt));
            }
            catch
            {
                return Request.CreateResponse(
                    HttpStatusCode.InternalServerError,
                    new Error("Server Error!", "The server has some problems."));
            }
        }

        [Route("app/setValue")]
        [HttpPost]
        public HttpResponseMessage setValue([FromBody] Device device)
        {
            String sqlStr = "SELECT DISTINCT hd.deviceId " +
                            "From Home.device hd " +
                            "INNER JOIN Home.GroupDevice gd ON gd.deviceId = hd.deviceId " +
                            "WHERE hd.deviceId = '" + device.homeDeviceId + "' AND gd.groupId = ( " +
                            "SELECT groupId " +
                            "FROM App.GroupAccount " +
                            "Where accountId = (" +
                            "SELECT accountId " +
                            "FROM App.Device " +
                            "WHERE accountId = '" + Encryption.unEncryption(device.accountToken) + "' and deviceId = '" +
                                Encryption.unEncryption(device.deviceId) + "' and status = 1) )";

            try
            {
                SqlDataAdapter da = new SqlDataAdapter(sqlStr, con);
                DataTable dt = new DataTable();
                da.Fill(dt);

                if (dt.Rows.Count == 0)
                {
                    return Request.CreateResponse(
                        HttpStatusCode.BadRequest,
                        new Error("Device error!", "This device is not access!"));
                }

                String sqlStr2 = "UPDATE Home.Device " +
                            "SET status = " + device.deviceStatus +
                            " WHERE deviceId = '" + device.homeDeviceId + "'";

                try
                {
                    con.Open();
                    SqlCommand cmd = new SqlCommand(sqlStr2, con);
                    cmd.ExecuteNonQuery();
                    con.Close();

                    return Request.CreateResponse(HttpStatusCode.OK, new Success(1, "Success"));

                }
                catch
                {
                    return Request.CreateResponse(
                        HttpStatusCode.InternalServerError,
                        new Error("Server Error!", "The server has some problems."));
                }

            }
            catch
            {
                return Request.CreateResponse(
                    HttpStatusCode.InternalServerError,
                    new Error("Server Error!", "The server has some problems."));
            }
        }

        [Route("app/getDevicePowerUsed")]
        [HttpPost]
        public HttpResponseMessage getDevicePowerUsed([FromBody] Device device)
        {
            String sqlStr = "SELECT DISTINCT hp.dateTime, hd.deviceId, hd.deviceName, hp.powerUsed " +
                            "From Home.device hd " +
                            "INNER JOIN Home.GroupDevice gd ON gd.deviceId = hd.deviceId " +
                            "INNER JOIN Home.PowerUsed hp ON gd.deviceId = hp.deviceId " +
                            "WHERE YEAR(dateTime) = YEAR(GETDATE()) AND gd.groupId = (" +
                            "SELECT groupId " +
                            "FROM App.GroupAccount " +
                            "Where accountId = (" +
                            "SELECT accountId " +
                            "FROM App.Device " +
                            "WHERE accountId = '" + Encryption.unEncryption(device.accountToken) + "' and deviceId = '" +
                                Encryption.unEncryption(device.deviceId) + "' and status = 1) )" +
                            "ORDER BY hp.dateTime DESC, hd.deviceId";

            try
            {
                SqlDataAdapter da = new SqlDataAdapter(sqlStr, con);
                DataTable dt = new DataTable();
                da.Fill(dt);

                if (dt.Rows.Count == 0)
                {
                    return Request.CreateResponse(
                        HttpStatusCode.BadRequest,
                        new Error("Device error!", "This device is not access!"));
                }

                return Request.CreateResponse(HttpStatusCode.OK, new Success(1, dt));

                String sqlStr2 = "UPDATE Home.Device " +
                            "SET status = " + device.deviceStatus +
                            " WHERE deviceId = '" + device.homeDeviceId + "'";

                try
                {
                    con.Open();
                    SqlCommand cmd = new SqlCommand(sqlStr2, con);
                    cmd.ExecuteNonQuery();
                    con.Close();

                    return Request.CreateResponse(HttpStatusCode.OK, new Success(1, "Success"));

                }
                catch
                {
                    return Request.CreateResponse(
                        HttpStatusCode.InternalServerError,
                        new Error("Server Error!", "The server has some problems."));
                }

            }
            catch(Exception e)
            {
                return Request.CreateResponse(
                    HttpStatusCode.InternalServerError,
                    /*new Error("Server Error!", "The server has some problems.")*/e.Message);
            }
        }
    }
}
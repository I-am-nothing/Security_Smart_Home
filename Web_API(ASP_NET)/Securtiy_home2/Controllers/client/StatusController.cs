using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Data;
using System.Data.SqlClient;
using Newtonsoft.Json;
using Securtiy_home2.Models;
using Version = Securtiy_home2.Models.Version;

namespace Securtiy_home2.Controllers {
    public class StatusController : ApiController {
        SqlConnection con = new SqlConnection(
            @"server=DESKTOP-IB1JUFQ\MSSQL2019;" +
            "database=Securicy_home;" +
            "Integrated Security=true;" +
            "Max Pool Size = 10000;");

        [Route("status")]
        [HttpGet]
        public HttpResponseMessage GetStatusHistory() {
            String sqlStr = "SELECT sqlVersion, homeVersion, appVersion, versionDescription " +
                                "FROM dbo.Version " +
                                "ORDER BY id DESC";

            try {
                SqlDataAdapter da = new SqlDataAdapter(sqlStr, con);
                DataTable dt = new DataTable();
                da.Fill(dt);

                return Request.CreateResponse(HttpStatusCode.OK, dt);
            }
            catch {
                return Request.CreateResponse(
                    HttpStatusCode.InternalServerError,
                    new Error("Server Error!", "The server has some problems."));
            }
        }

        [Route("status")]
        [HttpPost]
        public HttpResponseMessage CheckStatus([FromBody] Version version) {
            String sqlStr;
            String versionName = "";
            String versionValue = "";
            Byte flag = 0x0;

            if (version.appVersion != null) {
                versionName = "appVersion";
                versionValue = version.appVersion;
                flag++;
            }
            if (version.homeVersion != null) {
                versionName = "homeVersion";
                versionValue = version.homeVersion;
                flag++;
            }
            if (version.sqlVersion != null) {
                versionName = "sqlVersion";
                versionValue = version.sqlVersion;
                flag++;
            }
            if (flag >= 0x2) {
                return Request.CreateResponse(
                    HttpStatusCode.BadRequest,
                    new Error("Invalid syntax!", "You must only put one version in post."));
            }
            else if (flag == 0x0) {
                return Request.CreateResponse(
                    HttpStatusCode.BadRequest,
                    new Error("Invalid syntax!", "You must put one version in post."));
            }
            else if ((int)Convert.ToDouble(versionValue) == 0)
            {
                return Request.CreateResponse(HttpStatusCode.OK, new Success(0, "Did you hack my app??? :("));
            }
            sqlStr = "SELECT DISTINCT " + versionName +
                      " FROM dbo.Version " +
                      "WHERE " + versionName + " >= " + versionValue +
                      " ORDER BY " + versionName + " DESC";
            try {
                SqlDataAdapter da = new SqlDataAdapter(sqlStr, con);
                DataTable dt = new DataTable();
                da.Fill(dt);

                if (dt.Rows.Count == 1) {
                    return Request.CreateResponse(HttpStatusCode.OK, new Success(1, "Login success"));
                }
                else if (dt.Rows.Count > 1) {
                    if ((int)Convert.ToDouble(dt.Rows[0].ItemArray[0]) == (int)Convert.ToDouble(versionValue)) {
                        return Request.CreateResponse(
                            HttpStatusCode.OK,
                            new Success(2, "The new version is '" + dt.Rows[0].ItemArray[0] + "'. You can update the new version!"));
                    }
                    else {
                        return Request.CreateResponse(HttpStatusCode.OK, new Success(0, "Please update the new version"));
                    }
                }

                return Request.CreateResponse(HttpStatusCode.OK, new Success(0, "Did you hack my app??? :("));
            }
            catch {
                return Request.CreateResponse(
                    HttpStatusCode.InternalServerError,
                    new Error("Server Error!", "The server has some problems."));
            }
        }
    }
}

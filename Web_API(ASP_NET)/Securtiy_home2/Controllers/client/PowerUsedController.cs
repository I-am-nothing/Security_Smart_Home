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

namespace Security_home2.Controllers {
    public class PowerUsedController : ApiController {
        SqlConnection con = new SqlConnection(
            @"server=DESKTOP-IB1JUFQ\MSSQL2019;" +
            "database=Securicy_home;" +
            "Integrated Security=true;" +
            "Max Pool Size = 10000;");

        [Route("power/used")]
        [HttpPost]
        public HttpResponseMessage PostPowerUsed([FromBody] PowerUsedList list) {
            SqlDataAdapter da = new SqlDataAdapter(
                "SELECT datetime, powerUsed " +
                "FROM Home.powerUsed " +
                "WHERE " + list.select + " = '" + list.selectId + "'"
                , con);
            DataTable dt = new DataTable();
            da.Fill(dt);

            return Request.CreateResponse(HttpStatusCode.OK, dt);
        }
    }
}

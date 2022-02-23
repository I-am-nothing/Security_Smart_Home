using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using System.Data;

namespace Securtiy_home2.Models {
    public class Success {
        public Int16 status;
        public String message;
        public DataTable dataList;

        public Success(Int16 status, string message) {
            this.status = status;
            this.message = message;
        }

        public Success(Int16 status, DataTable dataList)
        {
            this.status = status;
            this.dataList = dataList;
        }
    }
}
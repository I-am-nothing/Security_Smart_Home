using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace Securtiy_home2.Models {
    public class Error {
        public String error;
        public String description;

        public Error(string error, string description) {
            this.error = error;
            this.description = description;
        }
    }
}
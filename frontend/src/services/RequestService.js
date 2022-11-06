import axios from "axios";

export default class RequestService {
  static axiosInstance = this.initAxiosInstance();

  static async createCorrection(data) {
    const result = await this.axiosInstance.put(`/realty/corrections/numeric`, data);
    if (result.data) {
      return result.data;
    }
  }

  static async editCorrection(data) {
    const result = await this.axiosInstance.patch(`/realty/corrections/numeric`, data);
    if (result.data) {
      return result.data;
    }
  }

  static async removeCorrection(id) {
    const result = await this.axiosInstance.delete(`/realty/corrections/numeric/${id}`, 
    );
    if (result.status === 200) {
      return true;
    }
    return false
  }

  static async getAllCorrections() {
    const result = await this.axiosInstance.get(`/realty/corrections/numeric`);
    if (result.data) {
      return result.data;
    }
  }

  static async exportPoolById(poolId, fileName) {
    const result = await this.axiosInstance.get(`/realty/objects/export/${poolId}`, {
      responseType: 'blob',
    })
    if (result.data) {
      console.log(result.data)
      const href = URL.createObjectURL(result.data);

      const link = document.createElement('a');
      link.href = href;
      link.setAttribute('download', `${fileName}.xlsx`);
      document.body.appendChild(link);
      link.click();

      document.body.removeChild(link);
      URL.revokeObjectURL(href);
    }
  }

  static async exportAllObjects(fileName) {
    const result = await this.axiosInstance.get(`/realty/objects/export`, {responseType: 'blob'})
    if (result.data) {
      console.log(result.data)
      const href = URL.createObjectURL(result.data);

      const link = document.createElement('a');
      link.href = href;
      link.setAttribute('download', `${fileName}.xlsx`);
      document.body.appendChild(link);
      link.click();

      document.body.removeChild(link);
      URL.revokeObjectURL(href);
    }
  }

  static async calculatePoolByIdAsync(id, useCorrections) {
    const result = await this.axiosInstance.post(`/realty/objects/calculatePool`,
    JSON.stringify({
      poolId: id,
      withCorrections: useCorrections
    }));
    if (result.data) {
      return result.data;
    }
  }

  static async geyObjectByIdAsync(id) {
    const result = await this.axiosInstance.get(`/realty/objects/${id}`);
    if (result.data) {
      return result.data;
    }
  }

  static async createPoolAsync(name) {
    console.log(name)
    const result = await this.axiosInstance.put(`/realty/pool`,
    name ? JSON.stringify({name: name}) : {},
    );
    if (result.data) {
      return result.data;
    }
  }

  static async loadPoolByIdAsync(id) {
    const result = await this.axiosInstance.get(`/realty/pool/${id}`);
    if (result.data) {
      return result.data;
    }
  }

  static async loadlAllPoolsAsync() {
    const result = await this.axiosInstance.get("/realty/pools");
    if (result.data) {
      return result.data;
    }
  }

  static async createRealtyObjectAsync(data) {
    const result = await this.axiosInstance.post("/realty/objects/create", 
      JSON.stringify(data)
    );
    if (result.data) {
      return result.data;
    }
  }

  static async editRealtyObjectAsync(data) {
    const result = await this.axiosInstance.patch("/realty/objects", 
      JSON.stringify(data)
    );
    if (result.status === 200) {
      return true;
    }
    return false
  }

  static async removeRealtyObjectAsync(id) {
    const result = await this.axiosInstance.delete(`/realty/objects/${id}`, 
    );
    if (result.status === 200) {
      return true;
    }
    return false
  }

  static initAxiosInstance() {
    return axios.create({
      withCredentials: true,
      headers: {
        userSessionId: this.getCookie('userSessionId')
      }
    })
  }

  static async registerAsync(data) {
    const result = await this.axiosInstance.post("/auth/register", 
      JSON.stringify(data)
    );
    if (result.data) {
      return result.data;
    }
  }

  static async authAsync(data) {
    const result = await this.axiosInstance.post("/auth/user", 
      JSON.stringify(data),
      {withCredentials: true}
    );
    if (result?.data?.sessionId) {
      this.setCookie('userSessionId', result.data.sessionId);
      this.axiosInstance = this.initAxiosInstance();
    }
  }

  static async loadFileAsync(data) {
    const result = await this.axiosInstance.put("/realty/objects/import",
      data,
    );
    if (result.data) {
      return result.data;
    }
  }

  static async getAllRealtyObjectsAsync() {
    const result = await this.axiosInstance.get("/realty/objects",
    );
    if (result.data) {
      return result.data;
    }
  }

  static setCookie(name, value, options = {}) {

    options = {
      path: '/',
      ...options
    };
  
    if (options.expires instanceof Date) {
      options.expires = options.expires.toUTCString();
    }
  
    let updatedCookie = encodeURIComponent(name) + "=" + encodeURIComponent(value);
  
    for (let optionKey in options) {
      updatedCookie += "; " + optionKey;
      let optionValue = options[optionKey];
      if (optionValue !== true) {
        updatedCookie += "=" + optionValue;
      }
    }
  
    document.cookie = updatedCookie;
  }

  static getCookie(name) {
    let matches = document.cookie.match(new RegExp(
      // eslint-disable-next-line no-useless-escape
      "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
  }

  static deleteCookie(name) {
    this.setCookie(name, "", {
      'max-age': -1
    })
  }

  // static async loadAccountsAsync() {
  //   try {
  //     const result = await axios.get("/accounts", {
  //       // headers: {
  //       //   ...this.config,
  //       // },
  //     });

  //     if (result.data) {
  //       return result.data.Data.Account;
  //     }
  //     console.log('Unknown error')
  //   } catch (e) {
  //     console.log(e);
  //   }
  // }

  // static async loadApplication(id) {
  //   try {
  //     const result = await axios.get(`/creditapplication/${id}`, {
  //     });

  //     if (result.data) {
  //       console.log(result.data)
  //       return result.data;
  //     }
  //     console.log('Unknown error')
  //   } catch (e) {
  //     console.log(e);
  //   }
  // }

  // static async removeApplication(id) {
  //   try {
  //     const result = await axios.delete(`/creditapplication/${id}`, {
  //     });

  //     if (result.status === 200) {
  //       return true;
  //     }
  //     console.log('Unknown error')
  //   } catch (e) {
  //     console.log(e);
  //   }
  // }

  // static async loadApplicationDetails(id) {
  //   try {
  //     const result = await axios.get(`/creditapplication/result/${id}`, {
  //     });

  //     if (result.data) {
  //       return result.data;
  //     }
  //     console.log('Unknown error')
  //   } catch (e) {
  //     console.log(e);
  //   }
  // }

  // static async createCreditApplication(body) {
  //     const result = await axios.post("/creditapplication", {
  //       ...body
  //     });

  //     if (result.data) {
  //       return result.data
  //     }
  // }
}
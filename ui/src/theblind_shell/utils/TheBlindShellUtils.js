export const reset = (data, reData) => {
  const keys = Object.keys(data);
  keys.forEach(item => {
    // eslint-disable-next-line no-param-reassign
    data[item] = reData[item];
  });
};

export const cloneObj = obj => {
  let newObj = {};
  if (obj instanceof Array) {
    newObj = [];
  }
  // eslint-disable-next-line guard-for-in,no-restricted-syntax
  for (const key in obj) {
    const val = obj[key];
    newObj[key] = typeof val === 'object' ? cloneObj(val) : val;
  }
  return newObj;
};
